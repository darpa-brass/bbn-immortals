package mil.darpa.immortals.flitcons.datatypes.hierarchical;

import mil.darpa.immortals.flitcons.Configuration;
import mil.darpa.immortals.flitcons.datatypes.DataType;
import mil.darpa.immortals.flitcons.datatypes.dynamic.Equation;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class HierarchicalDataTransformer {

	private enum TargetTransformation {
		Usage,
		Requirements,
		Interconnection
	}

//	private final Configuration.TransformationInstructions transformInstructions;

	private final HierarchicalDataContainer primaryData;

	private final HierarchicalDataContainer externalData;

	private final Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> primaryExternalRelationsMap;

	private final boolean preserveDebugRelations;

	public HierarchicalDataTransformer(@Nonnull HierarchicalDataContainer primaryData,
	                                   boolean preserveDebugRelations,
	                                   @Nullable HierarchicalDataContainer externalData,
	                                   @Nullable Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> primaryExternalRelationsMap) {
		this.primaryData = primaryData;
//		this.transformInstructions = Configuration.getInstance().transformation;
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

	private static void trimIgnoredNodeTrees(@Nonnull HierarchicalDataContainer data, @Nonnull Configuration.TransformationInstructions instructions) {
		for (List<String> originalList : instructions.ignoredNodes) {

			List<String> pathList = new ArrayList<>(originalList);
			String tail = pathList.remove(pathList.size() - 1);
			Set<HierarchicalData> dataSet = data.getNodesAtPath(pathList);

			for (HierarchicalData parentData : dataSet) {
				Iterator<HierarchicalData> iter = parentData.getChildrenDataIterator(tail);
				while (iter.hasNext()) {
					data.removeNodeTree(iter.next());
				}
			}
		}
		data.validate();
	}

	private static void trimIgnoredAttributes(@Nonnull HierarchicalDataContainer data, @Nonnull Configuration.TransformationInstructions instructions) {
		for (List<String> originalList : instructions.ignoredAttributes) {

			List<String> pathList = new ArrayList<>(originalList);
			String tail = pathList.remove(pathList.size() - 1);
			Set<HierarchicalData> dataSet = data.getNodesAtPath(pathList);

			for (HierarchicalData parentData : dataSet) {
				parentData.removeAttribute(tail);
			}
		}
		data.validate();
	}

	private static void renameAttributes(@Nonnull HierarchicalDataContainer data, @Nonnull Configuration.TransformationInstructions instructions) {
		for (String targetAttributeName : instructions.renameAttributes.keySet()) {
			List<String> nodePath = new LinkedList<>(instructions.renameAttributes.get(targetAttributeName));
			String tail = nodePath.remove(nodePath.size() - 1);
			Set<HierarchicalData> nodes = data.getNodesAtPath(nodePath);
			for (HierarchicalData node : nodes) {
				Object val;
				if ((val = node.getAttribute(tail)) != null) {
					node.removeAttribute(tail);
					node.addAttribute(HierarchicalIdentifier.createBlankNode(), targetAttributeName, val);
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
	private static void squashData(@Nonnull HierarchicalDataContainer data, @Nonnull Configuration.TransformationInstructions instructions, boolean preserveDebugRelations) {
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
		return produceResult(TargetTransformation.Interconnection);
	}

	public HierarchicalDataContainer produceInterconnectedRequirements() {
		return produceResult(TargetTransformation.Requirements);
	}

	public HierarchicalDataContainer produceInterconnectedUsage() {
		return produceResult(TargetTransformation.Usage);
	}

	private static void transform(@Nonnull HierarchicalDataContainer data,
	                              @Nonnull Configuration.TransformationInstructions instructions,
	                              boolean preserveDebugRelations) {
		trimOmittedNodeTrees(data, instructions);
		trimIgnoredNodeTrees(data, instructions);
		trimIgnoredAttributes(data, instructions);
		renameAttributes(data, instructions);
		squashData(data, instructions, preserveDebugRelations);
		trimEmptyBranches(data);
		injectCalculations(data, instructions);
		tagData(data, instructions);
	}

	private HierarchicalDataContainer produceResult(@Nonnull TargetTransformation targetTransformation) {

		DataType targetType;
		Configuration.TransformationInstructions specificInstructions;
		HierarchicalDataContainer primaryClone;
		HierarchicalDataContainer externalClone;


		switch (primaryData.dataType) {

			case RawInputExternalData:
				throw new AdaptationnException(ResultEnum.AdaptationUnexpectedError,
						"External data cannot be transformed independently of a configuration!");

			case RawInputConfigurationData:
				if (externalData == null) {
					throw new AdaptationnException(ResultEnum.AdaptationUnexpectedError,
							"Input Configuration with transformation " + targetTransformation.name() +
									" must be accompanied by external data!");
				} else if (externalData.dataType != DataType.RawInputExternalData) {
					throw AdaptationnException.internal("External data must be raw!");

				} else if (primaryExternalRelationsMap == null) {
					throw AdaptationnException.input("Cannot provide an external data hierarchy without external mapping information!");
				}

				switch (targetTransformation) {
					case Usage:
						targetType = DataType.InputInterconnectedUsageData;
//						specificInstructions = Configuration.getInstance() transformInstructions.usageTransformation;
						specificInstructions = Configuration.getInstance().getUsageTransformationInstructions();
						externalClone = externalData.duplicate(DataType.InputExternalUsageData);
						break;

					case Requirements:
						targetType = DataType.InputInterconnectedRequirementsData;
//						specificInstructions = transformInstructions.requirementsTransformation;
						specificInstructions = Configuration.getInstance().getRequirementsTransformationInstructions();
						externalClone = externalData.duplicate(DataType.InputExternalRequirementsData);
						break;

					case Interconnection:
						targetType = DataType.InputInterconnectedData;
						externalClone = externalData.duplicate();
						specificInstructions = Configuration.getInstance().getGlobalTransformationInstructions();
						break;

					default:
						throw AdaptationnException.internal("Unexpected target transformation '" + targetTransformation.name() + "'!");
				}
				break;

			case RawInventory:
				if (externalData != null) {
					throw AdaptationnException.internal("Inventory cannot be transformed using external data!");
				}

				if (targetTransformation == TargetTransformation.Requirements) {
					targetType = DataType.InventoryRequirementsData;
//					specificInstructions = transformInstructions.inventoryTransformation;
					specificInstructions = Configuration.getInstance().getInventoryTransformationInstructions();
					externalClone = null;
				} else {
					throw AdaptationnException.internal("Inventory Has no " + targetTransformation.name() +
							" transformation! Did you perhaps mean to use the Requirements transformation?");
				}
				break;

			default:
				throw new AdaptationnException(ResultEnum.AdaptationUnexpectedError,
						"Cannot transform previously interconnected or transformed data type '" + primaryData.dataType + "'!");
		}

		primaryClone = primaryData.duplicate(targetType);

		if (externalClone != null && targetTransformation != TargetTransformation.Interconnection) {
			transform(externalClone, specificInstructions, preserveDebugRelations);
		}

		pullExternalDataIntoDaus(primaryClone, externalClone, primaryExternalRelationsMap);

		if (targetTransformation != TargetTransformation.Interconnection) {
			transform(primaryClone, specificInstructions, preserveDebugRelations);
		}
		return primaryClone;
	}

	private static void injectCalculations(@Nonnull HierarchicalDataContainer data, @Nonnull Configuration.TransformationInstructions instructions) {
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
}
