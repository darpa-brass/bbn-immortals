package mil.darpa.immortals.flitcons.datatypes.hierarchical;

import mil.darpa.immortals.flitcons.Configuration;
import mil.darpa.immortals.flitcons.datatypes.DataType;
import mil.darpa.immortals.flitcons.datatypes.dynamic.Equation;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class HierarchicalDataTransformer {

	private final Configuration.TransformationInstructions transformInstructions;

	private final HierarchicalDataContainer primaryData;

	private final HierarchicalDataContainer externalData;

	private final Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> primaryExternalRelationsMap;

	private final boolean preserveDebugRelations;

	public static boolean ignoreEquations = false;

	public HierarchicalDataTransformer(@Nonnull HierarchicalDataContainer primaryData,
	                                   @Nonnull Configuration.TransformationInstructions transformInstructions,
	                                   boolean preserveDebugRelations,
	                                   @Nullable HierarchicalDataContainer externalData,
	                                   @Nullable Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> primaryExternalRelationsMap) {
		this.primaryData = primaryData;
		this.transformInstructions = transformInstructions;
		this.preserveDebugRelations = preserveDebugRelations;
		this.externalData = externalData;
		this.primaryExternalRelationsMap = primaryExternalRelationsMap;
	}

	/**
	 * @param node             The root node
	 * @param nodeCollection   The collection to add nodes to that pass the predicate check
	 * @param addToCollection  The predicate check
	 * @param propagateUpFalse If a child node fails, propagate that failure up the parent stack
	 */
	private static boolean collectNodesDFS(HierarchicalData node, List<HierarchicalData> nodeCollection,
	                                       boolean propagateUpFalse, Predicate<HierarchicalData> addToCollection) {
		boolean added = false;

		if (addToCollection.test(node)) {
			added = true;
		}

		Iterator<String> childTypeIter = node.getChildrenClassIterator();
		while (childTypeIter.hasNext()) {
			String childType = childTypeIter.next();

			Iterator<HierarchicalData> childIter = node.getChildrenDataIterator(childType);
			while (childIter.hasNext()) {
				HierarchicalData child = childIter.next();
				if (!collectNodesDFS(child, nodeCollection, propagateUpFalse, addToCollection) && propagateUpFalse) {
					added = false;
				}
			}
		}

		if (added && !nodeCollection.contains(node)) {
			nodeCollection.add(node);
		}
		return added;
	}


	/**
	 * Removes nodes that are of specific types of nodes that should be ignored
	 */
	private static void trimIgnoredNodeTrees(@Nonnull HierarchicalDataContainer data, @Nonnull Configuration.TransformationInstructions instructions) {
		List<HierarchicalData> nodesToRemove = new LinkedList<>();

		Set<String> ignoreNodes = instructions.ignoredNodes;

		for (HierarchicalData root : data.getDauRootNodes()) {
			collectNodesDFS(root, nodesToRemove, false, a ->
					ignoreNodes.contains(a.getNodeType())
			);
		}

		for (HierarchicalData node : nodesToRemove) {
			data.removeNodeTree(node);
		}
		data.validate();
	}

	//	todo: finish making sure conflicting values here are properly removed!
	private static void trimIgnoredAttributes(@Nonnull HierarchicalDataContainer data, @Nonnull Configuration.TransformationInstructions instructions) {

		Map<String, List<List<String>>> ignoredAttributeMapping;

		if (data.dataType.isValidConfiguration) {
			ignoredAttributeMapping = instructions.ignoredValidConfigurationAttributes;

		} else if (data.dataType.isFaultyConfiguration) {
			ignoredAttributeMapping = instructions.ignoredFaultyConfigurationAttributes;

		} else if (data.dataType.isInventory) {
			ignoredAttributeMapping = instructions.ignoredInventoryAttributes;

		} else {
			throw AdaptationnException.internal("Cannot trim ignored attributes on type '" + data.dataType.name() + "'!");
		}

		if (data.dataType.isRaw) {
			throw AdaptationnException.internal("Raw data should not be trimmed!");
		}

		List<HierarchicalData> allNodes = new LinkedList<>();

		for (HierarchicalData root : data.getDauRootNodes()) {
			collectNodesDFS(root, allNodes, false, a -> true);
		}

		Set<String> attrs = new HashSet<>();

		for (HierarchicalData node : allNodes) {
			attrs.clear();
			attrs.addAll(node.getAttributeNames());

			attrLoop:
			for (String attributeName : attrs) {
				if (ignoredAttributeMapping.containsKey(attributeName)) {
					treeloop:
					for (List<String> ignoredTree : ignoredAttributeMapping.get(attributeName)) {

						if (ignoredTree.get(ignoredTree.size() - 1).equals(node.getNodeType())) {

							ListIterator<String> iterator = ignoredTree.listIterator(ignoredTree.size() - 1);

							HierarchicalData currentNode = node;
							String currentCheckValue;

							while (iterator.hasPrevious()) {
								if (currentNode.parentNode == null) {
									continue treeloop;

								} else {
									currentNode = currentNode.getParentData();
									currentCheckValue = iterator.previous();


									if (currentCheckValue.equals(currentNode.getNodeType())) {
										if (!iterator.hasPrevious()) {
											node.removeAttribute(attributeName);
										}

									} else {
										continue treeloop;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Removes nodes that should be omitted due to lacking a specific property
	 */
	private static void trimOmittedNodeTrees(@Nonnull HierarchicalDataContainer data, @Nonnull Configuration.TransformationInstructions instructions) {
		List<HierarchicalData> nodesToRemove = new LinkedList<>();

		for (HierarchicalData root : data.getDauRootNodes()) {
			collectNodesDFS(root, nodesToRemove, false, a ->
					{
						if (instructions.ignoreParentsWithoutProperties.containsKey(a.getNodeType())) {

							HierarchicalData currentNode = a;
							Iterator<String> targetPath = instructions.ignoreParentsWithoutProperties.get(a.getNodeType()).iterator();

							while (targetPath.hasNext()) {
								String childNodeType = targetPath.next();

								if (targetPath.hasNext()) {
									Iterator<HierarchicalData> childrenIterator = currentNode.getChildrenDataIterator(childNodeType);

									if (!childrenIterator.hasNext()) {
										return true;
									} else {
										currentNode = childrenIterator.next();

										if (childrenIterator.hasNext()) {
											throw AdaptationnException.input("Multiple children of the same type are not currently supported for invalidating nodes!");
										}
									}

								} else {
									if (currentNode.getAttribute(childNodeType) == null) {
										return true;
									}
								}
							}
						}
						return false;
					}
			);
		}

		for (HierarchicalData node : nodesToRemove) {
			data.removeNodeTree(node);
		}
		data.validate();
	}

	/**
	 * recursively squashes the properties of specific nodes into their parent to remove redundant layering
	 */
	private void squashData(@Nonnull HierarchicalDataContainer data, @Nonnull Configuration.TransformationInstructions instructions) {
		LinkedList<HierarchicalData> markedForSquashing = new LinkedList<>();
		boolean modified = true;

		// For all nodes
		while (modified) {
			modified = false;
			Iterator<HierarchicalData> originalDataIter = data.getDataIterator();

			while (originalDataIter.hasNext()) {
				HierarchicalData candidateNode = originalDataIter.next();

				// If the node type is flagged for squashing and it has not been marked for squashing and is not a root node
				if (candidateNode.getParent() != null && instructions.shortNodesToParent.containsKey(candidateNode.getParent().getNodeType()) &&
						instructions.shortNodesToParent.get(candidateNode.getParent().getNodeType()).contains(candidateNode.getNodeType()) &&
						!markedForSquashing.contains(candidateNode) && !candidateNode.isRootNode()) {

//                 TODO: Add clobber check via configuration values

					// Mark it for squashing if debug information is not being preserved or there is no debug info
					if (!preserveDebugRelations || candidateNode.getDebugLabel() == null) {
						markedForSquashing.add(candidateNode);
						// And mark the loop as modified so continued attribute squishing can occur
						modified = true;
					}
				}
			}
		}

		Collections.reverse(markedForSquashing);
		for (HierarchicalData node : markedForSquashing) {
			data.removeAndShortParentAndChildNodes(node, instructions, preserveDebugRelations);
		}
		data.validate();
	}


	private static void tagData(@Nonnull HierarchicalDataContainer data, @Nonnull Configuration.TransformationInstructions instructions) {
		Set<String> tagNodes = instructions.taggedNodes;

		for (HierarchicalIdentifier identifier : data.getNodeIdentifiers()) {
			HierarchicalData node = data.getNode(identifier);
			if (tagNodes.contains(node.getNodeType())) {
				node.exposeTag();
			}
		}
		data.validate();
	}

	private static void trimEmptyBranches(HierarchicalDataContainer data) {
		List<HierarchicalData> nodesToRemove = new LinkedList<>();

		Set<HierarchicalIdentifier> localNodeIdentifiers = data.getNodeIdentifiers();

		// For all root nodes
		for (HierarchicalData root : data.getDauRootNodes()) {
			// Iterate through the children, keeping it if any children in the tree should be kept
			collectNodesDFS(root, nodesToRemove, true, a ->
					{
						// Iterate through their children and assume removal
						boolean removeNode = true;

						// Unless they contain attributes
						if (a.getAttributeNames().size() > 0) {
							removeNode = false;
						}

						// Contain external inbound references
						Iterator<HierarchicalIdentifier> iter = a.getInboundReferencesIterator();
						while (iter.hasNext()) {
							HierarchicalIdentifier node = iter.next();
							if (!localNodeIdentifiers.contains(node)) {
								removeNode = false;
							}
						}

						// Or contain external outbound references
						iter = a.getOutboundReferencesIterator();
						while (iter.hasNext()) {
							HierarchicalIdentifier node = iter.next();
							if (!localNodeIdentifiers.contains(node)) {
								removeNode = false;
							}
						}
						return removeNode;
					}
			);
		}

		// Then remove each node
		for (HierarchicalData node : nodesToRemove) {
			data.removeNode(node);
		}
		data.validate();
	}

	/**
	 * Produces a {@link HierarchicalDataContainer} that has the external data connected to the primary data
	 */
	public HierarchicalDataContainer produceInterconnectedResult() {
		return produceResult(false);
	}

	/**
	 * Produces a {@link HierarchicalDataContainer} that has the external data connected to the primary data and has
	 * all transformations applied to it
	 */
	public HierarchicalDataContainer produceTransformedInterconnectedResult() {
		return produceResult(true);
	}

	private HierarchicalDataContainer produceResult(boolean applyTransformation) {
		DataType targetType;
		switch (primaryData.dataType) {
			case Inventory_Raw:
				if (externalData != null) {
					throw new AdaptationnException(ResultEnum.AdaptationUnexpectedError,
							"Inventory cannot be transformed using external data!");
				}
				targetType = DataType.Inventory_Transformed;
				break;

			case FaultyConfiguration_Raw:
				if (externalData == null) {
					throw new AdaptationnException(ResultEnum.AdaptationUnexpectedError,
							"Faulty Configuration data must be accompanied by external data!");
				} else if (externalData.dataType != DataType.FaultyConfigurationExternalData_Raw) {
					throw new AdaptationnException(ResultEnum.AdaptationUnexpectedError,
							"Faulty Configuration must be accompanied by raw external data!");
				}
				if (applyTransformation) {
					targetType = DataType.FaultyConfiguration_InterconnectedTransformed;
				} else {
					targetType = DataType.FaultyConfiguration_Interconnected;
				}

				break;

			case FaultyConfigurationExternalData_Raw:
			case FaultyConfigurationExternalData_Transformed:
			case ValidConfigurationExternalData_Raw:
			case ValidConfigurationExternalData_Transformed:
				throw new AdaptationnException(ResultEnum.AdaptationUnexpectedError,
						"External data cannot be transformed independently of a configuration!");

			case ValidConfiguration_Raw:
				if (externalData == null) {
					throw new AdaptationnException(ResultEnum.AdaptationUnexpectedError,
							"Valid Configuration data must be accompanied by external data!");
				} else if (externalData.dataType != DataType.ValidConfigurationExternalData_Raw) {
					throw new AdaptationnException(ResultEnum.AdaptationUnexpectedError,
							"Valid Configuration must be accompanied by raw external data!");
				}
				if (applyTransformation) {
					targetType = DataType.ValidConfiguration_InterconnectedTransformed;
				} else {
					targetType = DataType.ValidConfiguration_Interconnected;
				}
				break;

			case Inventory_Transformed:
			case FaultyConfiguration_Interconnected:
			case FaultyConfiguration_InterconnectedTransformed:
			case ValidConfiguration_Interconnected:
			case ValidConfiguration_InterconnectedTransformed:
				throw new AdaptationnException(ResultEnum.AdaptationUnexpectedError,
						"Cannot transform previously interconnected or transformed data type '" + primaryData.dataType + "'!");

			default:
				throw new AdaptationnException(ResultEnum.AdaptationUnexpectedError,
						"No valid transformation could be found for configuration=" + primaryData.dataType.name() +
								", externalData=" + (externalData == null ? "null" : externalData.dataType.name()) + "!");
		}

		HierarchicalDataContainer primaryClone = primaryData.duplicate(targetType);
		HierarchicalDataContainer externalClone;

		if (externalData != null || primaryExternalRelationsMap != null) {
			if (externalData == null) {
				throw AdaptationnException.input("Cannot provide external mapping information without an external data hierarchy!");
			}

			if (primaryExternalRelationsMap == null) {
				throw AdaptationnException.input("Cannot provide an external data hierarchy without external mapping information!");
			}


			if (externalData.dataType == DataType.FaultyConfigurationExternalData_Raw) {
				externalClone = externalData.duplicate(DataType.FaultyConfigurationExternalData_Transformed);

			} else if (externalData.dataType == DataType.ValidConfigurationExternalData_Raw) {
				externalClone = externalData.duplicate(DataType.ValidConfigurationExternalData_Transformed);

			} else {
				throw AdaptationnException.internal("Invalid external data of type '" + externalData.dataType.name() + "'!");
			}

			if (applyTransformation) {
				trimIgnoredAttributes(externalClone, transformInstructions);
				trimOmittedNodeTrees(externalClone, transformInstructions);
				squashData(externalClone, transformInstructions);
				trimEmptyBranches(externalClone);
				trimIgnoredNodeTrees(externalClone, transformInstructions);
				if (!ignoreEquations) {
					injectCalculations(externalClone, transformInstructions);
				}
				tagData(externalClone, transformInstructions);
			}

			pullExternalDataIntoDaus(primaryClone, externalClone, primaryExternalRelationsMap);
		}

		if (applyTransformation) {
			trimIgnoredAttributes(primaryClone, transformInstructions);
			trimOmittedNodeTrees(primaryClone, transformInstructions);
			squashData(primaryClone, transformInstructions);
			trimEmptyBranches(primaryClone);
			trimIgnoredNodeTrees(primaryClone, transformInstructions);
			if (!ignoreEquations) {
				injectCalculations(primaryClone, transformInstructions);
			}
			tagData(primaryClone, transformInstructions);
		}

		return primaryClone;
	}

	public static void injectCalculations(@Nonnull HierarchicalDataContainer data, @Nonnull Configuration.TransformationInstructions instructions) {
		Map<String, List<Configuration.Calculation>> calculationsMap = instructions.calculations;

		Iterator<HierarchicalData> originalDataIter = data.getDataIterator();

		Map<HierarchicalIdentifier, Set<Configuration.Calculation>> equationsToInject = new HashMap<>();

		while (originalDataIter.hasNext()) {
			HierarchicalData node = originalDataIter.next();

			List<Configuration.Calculation> calculations;
			if ((calculations = calculationsMap.get(node.getNodeType())) != null) {
				for (Configuration.Calculation calculation : calculations) {
					if (node.getAttribute(calculation.targetValueIdentifier) == null && !node.getChildrenDataIterator(calculation.targetValueIdentifier).hasNext()) {
						for (String substitutionValue : calculation.substitutionValues) {
							if (node.getAttribute(substitutionValue) == null && !node.getChildrenDataIterator(substitutionValue).hasNext()) {
								throw AdaptationnException.internal(
										"A node of type '" + node.getNodeType() +
												"' has a calculation to determine the value of '" + calculation.targetValueIdentifier +
												"' but is missing value '" + substitutionValue + "' necessary to compute it!");
							}
						}
						Set<Configuration.Calculation> equations = equationsToInject.computeIfAbsent(node.node, k -> new HashSet<>());
						equations.add(calculation);
						break;
					}
				}
			}
		}

		for (HierarchicalIdentifier node : equationsToInject.keySet()) {
			Set<Configuration.Calculation> calcs = equationsToInject.get(node);
			for (Configuration.Calculation calc : calcs) {
				data.injectEquation(node, calc.targetValueIdentifier, new Equation(calc.equation));
			}
		}
	}

	private static void pullExternalDataIntoDaus(@Nonnull HierarchicalDataContainer primaryStructure,
	                                             @Nonnull HierarchicalDataContainer externalStructure,
	                                             @Nonnull Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> indirectRelations) {

		Map<HierarchicalData, Set<HierarchicalData>> newParentChildrenMap = new HashMap<>();

		// For each DAU node
		for (HierarchicalData dauNode : primaryStructure.getDauRootNodes()) {

			// Get all child node data related to that dau node
			Collection<HierarchicalData> dauNodeSet = primaryStructure.getDauNodes(dauNode);

			// And for each one
			for (HierarchicalData primaryNode : dauNodeSet) {

				// For any inbound reference
				Iterator<HierarchicalIdentifier> iter = primaryNode.getInboundReferencesIterator();
				while (iter.hasNext()) {
					HierarchicalIdentifier hn = iter.next();

					// That does not reference another child of the same DAU
					if (dauNodeSet.stream().noneMatch(t -> t.node.equals(hn))) {

						// If no external relation to it can be found, throw an exception
						if (!indirectRelations.containsKey(hn)) {
							throw AdaptationnException.input("Reference edge does not map to any corresponding references to known data!");
						}

						// Otherwise, get the related nodes
						Set<HierarchicalIdentifier> relatedNodes = indirectRelations.get(hn);
						for (HierarchicalIdentifier referencedNode : relatedNodes) {
							// And for each one, confirm they have related data
							if (!externalStructure.containsNode(referencedNode)) {
								throw AdaptationnException.input("Could not find a matching external data node for the node '" + referencedNode + "'!");
							}

							// And set the parent of those nodes to the DAU node
							HierarchicalData externalNode = externalStructure.getNode(referencedNode);
							Set<HierarchicalData> parentChildren = newParentChildrenMap.computeIfAbsent(primaryNode, k -> new HashSet<>());
							parentChildren.add(externalNode);

						}
					}
				}

				// For any outbound reference
				iter = primaryNode.getOutboundReferencesIterator();
				while (iter.hasNext()) {
					HierarchicalIdentifier hn = iter.next();
					if (!primaryStructure.containsNode(hn)) {
						// Throw an exception because that is not yet supported.
						throw AdaptationnException.input("Reference from primary Node '" + primaryNode.toString() + "' to external node '" + hn.toString() + "' detected! This is not supported!");
					}
				}
			}
		}

		// Then remap the nodes;
		for (Map.Entry<HierarchicalData, Set<HierarchicalData>> entry : newParentChildrenMap.entrySet()) {
			HierarchicalData parentNode = entry.getKey();
			for (HierarchicalData externalChild : entry.getValue()) {
				primaryStructure.cloneTreeIntoContainer(externalChild, parentNode);
			}
		}
	}

	public static void displayHierarchicalDataContainer(HierarchicalDataContainer container, @Nullable File targetFile) {
		List<String> lines = new LinkedList<>();
		for (HierarchicalData dauNode : container.getDauRootNodes()) {
			System.out.println(dauNode.toString());
			lines.add(dauNode.toString());
			Collection<HierarchicalData> nds = container.getDauNodes(dauNode);

			for (HierarchicalData nd : nds) {
				StringBuilder pathStringBuilder = new StringBuilder();

				for (HierarchicalData hn : nd.getPathAsData()) {
					pathStringBuilder.append(hn.toString()).append(".");
				}
				String pathString = pathStringBuilder.append(nd.toString()).append(".").toString();

				for (String key : nd.getAttributeNames()) {
					lines.add("\t" + pathString + key + " = " + nd.getAttribute(key));
					System.out.println("\t" + pathString + key + " = " + nd.getAttribute(key));
				}
			}
		}
		if (targetFile != null) {
			try {
				Collections.sort(lines);
				FileUtils.writeLines(targetFile, lines);
			} catch (IOException e) {
				throw AdaptationnException.internal(e);
			}
		}
	}
}
