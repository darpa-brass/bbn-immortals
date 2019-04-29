package mil.darpa.immortals.flitcons.datatypes.hierarchical;

import mil.darpa.immortals.flitcons.Configuration;
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

	public HierarchicalDataTransformer(@Nonnull HierarchicalDataContainer primaryData,
	                               @Nonnull Configuration.TransformationInstructions transformInstructions,
	                               @Nullable HierarchicalDataContainer externalData,
	                               @Nullable Map<HierarchicalIdentifier, Set<HierarchicalIdentifier>> primaryExternalRelationsMap) {
		this.primaryData = primaryData;
		this.transformInstructions = transformInstructions;
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

		Set<String> ignoreNodes = instructions.ignoreNodes;

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
											throw new RuntimeException("Multiple children of the same type are not currently supported for invalidating nodes!");
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
	private static void squashData(@Nonnull HierarchicalDataContainer data, @Nonnull Configuration.TransformationInstructions instructions) {
		LinkedList<HierarchicalData> markedForSquashing = new LinkedList<>();
		boolean modified = true;

		// For all nodes
		while (modified) {
			modified = false;
			Iterator<HierarchicalData> originalDataIter = data.getDataIterator();

			while (originalDataIter.hasNext()) {
				HierarchicalData candidateNode = originalDataIter.next();

				// If the node type is flagged for squashing and it has not been marked for squashing and is not a root node
				if (instructions.shortNodesToParent.contains(candidateNode.getNodeType()) &&
						!markedForSquashing.contains(candidateNode) && !candidateNode.isRootNode()) {

//                 TODO: Add clobber check via configuration values

					// Mark it for squashing
					markedForSquashing.add(candidateNode);
					// And mark the loop as modified so continued attribute squishing can occur
					modified = true;
				}
			}
		}

		Collections.reverse(markedForSquashing);
		for (HierarchicalData node :  markedForSquashing) {
			data.removeAndShortParentAndChildNodes(node, instructions);
		}
		data.validate();
	}


	private static void tagData(@Nonnull HierarchicalDataContainer data, @Nonnull Configuration.TransformationInstructions instructions) {
		Set<String> tagNodes = instructions.taggedNodes;

		for (HierarchicalIdentifier identifier : data.getNodeIdentifiers()) {
			HierarchicalData node = data.getNode(identifier);
			if (tagNodes.contains(node.getNodeType())) {
				node.setTag();
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
//            data.removeNodeTree(node);
		}
		data.validate();
	}

	public HierarchicalDataContainer produceResult() {


		HierarchicalDataContainer primaryClone = primaryData.duplicate();
		HierarchicalDataContainer externalClone;

		if (externalData != null || primaryExternalRelationsMap != null) {
			if (externalData == null) {
				throw new RuntimeException("Cannot provide external mapping information with an external data hierarchy!");
			}

			if (primaryExternalRelationsMap == null) {
				throw new RuntimeException("Cannot provide an external data hierarchy without external mapping information!");
			}

			externalClone = externalData.duplicate();
			trimOmittedNodeTrees(externalClone, transformInstructions);
			squashData(externalClone, transformInstructions);
			trimEmptyBranches(externalClone);
			trimIgnoredNodeTrees(externalClone, transformInstructions);
			tagData(externalClone, transformInstructions);

			pullExternalDataIntoDaus(primaryClone, externalClone, primaryExternalRelationsMap);
		}

		trimOmittedNodeTrees(primaryClone, transformInstructions);
		squashData(primaryClone, transformInstructions);
		trimEmptyBranches(primaryClone);
		trimIgnoredNodeTrees(primaryClone, transformInstructions);
		tagData(primaryClone, transformInstructions);

		return primaryClone;
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
							throw new RuntimeException("Reference edge does not map to any corresponding references to known data!");
						}

						// Otherwise, get the related nodes
						Set<HierarchicalIdentifier> relatedNodes = indirectRelations.get(hn);
						for (HierarchicalIdentifier referencedNode : relatedNodes) {
							// And for each one, confirm they have related data
							if (!externalStructure.containsNode(referencedNode)) {
								throw new RuntimeException("Could not find a matching external data node for the node '" + referencedNode + "'!");
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
						throw new RuntimeException("Reference from primary Node '" + primaryNode.toString() + "' to external node '" + hn.toString() + "' detected! This is not supported!");
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
				throw new RuntimeException(e);
			}
		}
	}
}
