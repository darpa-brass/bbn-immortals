package mil.darpa.immortals.flitcons.datatypes.hierarchical;

import mil.darpa.immortals.flitcons.Configuration;
import mil.darpa.immortals.flitcons.Utils;
import mil.darpa.immortals.flitcons.datatypes.DataType;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A container for a known set of hierarchical data. The intent is to know when data exists that has not been
 * incorporated into a hierarchy or if it has come into an invalid state.
 */
public class HierarchicalDataContainer implements DuplicateInterface<HierarchicalDataContainer> {

	private static final Logger logger = LoggerFactory.getLogger(HierarchicalDataContainer.class);

	private final LinkedHashMap<HierarchicalIdentifier, HierarchicalData> existingDataIdentifierMap;

	private final Map<HierarchicalData, List<HierarchicalData>> superParentChildElements;

	public final DataType dataType;

	private boolean validated = false;

	public HierarchicalDataContainer(@Nonnull DataType dataType, @Nonnull LinkedHashMap<HierarchicalIdentifier, HierarchicalData> identifierDataMap, @Nonnull LinkedHashMap<HierarchicalData, List<HierarchicalData>> rootChildValues) {
		this.dataType = dataType;
		this.existingDataIdentifierMap = new LinkedHashMap<>();
		this.existingDataIdentifierMap.putAll(identifierDataMap);
		this.superParentChildElements = new HashMap<>(rootChildValues);

		for (HierarchicalData data : existingDataIdentifierMap.values()) {
			data.setParentContainer(this);
		}
	}

//	public Set<HierarchicalData> checkForDummyNodes() {
//		int emptyNodeCount = 0;
//		Set<HierarchicalData> emptyNodes = new HashSet<>();
//		Iterator<HierarchicalData> dataIterator = getDataIterator();
//		while (dataIterator.hasNext()) {
//			HierarchicalData data = dataIterator.next();
//			if (data.getAssociatedObject() == null) {
//			} else if (!(data.getAssociatedObject() instanceof OrientVertex)) {
//				emptyNodes.add(data);
//				emptyNodeCount++;
//			}
//		}
//		System.out.println("EmptyNodeCount: " + emptyNodeCount);
//		return emptyNodes;
//	}

	//	 Useful functions for debugging node trees since the relations are through identifiers and not direct
	private Map<HierarchicalData, Map> debugMap;

	public final void fillDebugMap() {
		Map<HierarchicalData, Map> targetMap = debugMap = new HashMap<>();

		for (HierarchicalData parent : superParentChildElements.keySet()) {
			targetMap.put(parent, getChildrenMap(parent));
		}
	}

	private Map getChildrenMap(HierarchicalData node) {
		Map<HierarchicalData, Object> rval = new HashMap<>();

		Iterator<String> childTypeIter = node.getChildrenClassIterator();
		while (childTypeIter.hasNext()) {
			String childType = childTypeIter.next();

			Iterator<HierarchicalData> childIter = node.getChildrenDataIterator(childType);
			while (childIter.hasNext()) {
				HierarchicalData child = childIter.next();
				rval.put(child, getChildrenMap(child));
			}
		}
		return rval;
	}

	//	End useful functions

	HierarchicalData getData(@Nonnull HierarchicalIdentifier parentIdentifier) {
		return existingDataIdentifierMap.get(parentIdentifier);
	}

	private void iterateAndRemove(HierarchicalData parent, Set<HierarchicalData> allParentChildNodes, Set<HierarchicalData> allKnownNodes) {
		validated = false;
		Iterator<String> childClassIterator = parent.getChildrenClassIterator();
		while (childClassIterator.hasNext()) {
			String nodeClass = childClassIterator.next();
			Iterator<HierarchicalData> childIterator = parent.getChildrenDataIterator(nodeClass);
			while (childIterator.hasNext()) {
				HierarchicalData child = childIterator.next();
				iterateAndRemove(child, allParentChildNodes, allKnownNodes);
			}
		}

		if (allParentChildNodes.contains(parent)) {
			allParentChildNodes.remove(parent);
		} else {
			throw AdaptationnException.internal("Cannot find matching node in parent child nodes for '" + parent.toString() + "'!");
		}

		if (allKnownNodes.contains(parent)) {
			allKnownNodes.remove(parent);
		} else {
			throw AdaptationnException.internal("Cannot find matching node in all known nodes for '" + parent.toString() + "'!");
		}
	}

	public final void validate() {
		if (validated) {
			logger.warn("Validation has already been performed!");
			return;
		}
		Set<HierarchicalData> allChildNodes = new HashSet<>();

		// For each root object
		for (HierarchicalData rootData : superParentChildElements.keySet()) {

			// Add it to all known nodes
			allChildNodes.add(rootData);

			if (!existingDataIdentifierMap.containsKey(rootData.node)) {
				// And if the identifier isn't in the identifier -> data map, throw an exception
				throw AdaptationnException.internal("Matching key not found for node associated with root node!");
			} else if (existingDataIdentifierMap.get(rootData.node) != rootData) {
				// And if InternalException value doesn't match the value for that isn't in the identifier -> data map, throw an exception
				throw AdaptationnException.internal("Matching object not found for node associated with root node!");
			}

			// And for each child node
			List<HierarchicalData> childNodes = superParentChildElements.get(rootData);
			for (HierarchicalData src : childNodes) {
				// Add it to all known nodes
				allChildNodes.add(src);

				if (!existingDataIdentifierMap.containsKey(src.node)) {
					// And if the identifier isn't in the identifier -> data map, throw an exception
					throw AdaptationnException.internal("Matching key not found for node associated with root node!");
				} else if (existingDataIdentifierMap.get(src.node) != src) {
					// And if the value doesn't match the value for that isn't in the identifier -> data map, throw an exception
					throw AdaptationnException.internal("Matching object not found for node associated with root node!");
				}
			}
		}

		// Then, for all known identifiers
		for (HierarchicalIdentifier keyObject : existingDataIdentifierMap.keySet()) {
			HierarchicalData value = existingDataIdentifierMap.get(keyObject);

			if (value.node != keyObject) {
				throw AdaptationnException.internal("Data container record of key -> value mapping for all nodes is inconsistent!");
			}

			// If they weren't in the list of previously collected nodes, throw an exception
			if (!allChildNodes.contains(value)) {
				throw AdaptationnException.internal("Not all nodes contained in the main node map have an identified root node!");
			}
		}


		// Below can probably be consolidated into above somehow.
		Set<HierarchicalData> collectedParentChildNodes = new HashSet<>();
		Set<HierarchicalData> allKnownNodes = new HashSet<>(existingDataIdentifierMap.values());

		// For each parent node
		for (HierarchicalData parent : superParentChildElements.keySet()) {
			// If a parent node set already exists, thr4ow an exception
			if (!collectedParentChildNodes.isEmpty()) {
				String errorValues = collectedParentChildNodes.stream().map(HierarchicalData::toString).collect(Collectors.joining());
				throw AdaptationnException.internal("Known child nodes have not been iterated! Details:\n" + errorValues);
			}

			collectedParentChildNodes.add(parent);
			collectedParentChildNodes.addAll(superParentChildElements.get(parent));
			iterateAndRemove(parent, collectedParentChildNodes, allKnownNodes);
		}

		if (!allKnownNodes.isEmpty()) {
			String errorValues = allKnownNodes.stream().map(x -> x.getNodeType() + "(" + x.toString() + ")\n").collect(Collectors.joining());
			throw AdaptationnException.internal("Known nodes not been iterated! Details:\n" + errorValues);
		}
		validated = true;
	}

	/**
	 * Clones a full tree into the container
	 *
	 * @param sourceData       the node tree to copy into the container
	 * @param targetParentNode The immediate parent object that the node should be a child of
	 */
	public void cloneTreeIntoContainer(@Nonnull HierarchicalData sourceData,
	                                   @Nonnull HierarchicalData targetParentNode) {
		validated = false;
		if (existingDataIdentifierMap.containsValue(sourceData)) {
			throw AdaptationnException.internal("Cannot insert and clone a node from the same database!");

		} else if (existingDataIdentifierMap.containsKey(sourceData.node)) {
			// TODO: Enforce proper cardinality

			if (sourceData.node.referenceIdentifier == null) {
				throw AdaptationnException.internal("Node already exists!");
			}
		}

		// Duplicate the node
		HierarchicalData clone = sourceData.duplicate();
		clone.setParentContainer(this);
		clone.isRootNode = false;
		if (clone.getParent() != null) {
			clone.clearParentNode();
		}

		setParentNode(clone, targetParentNode);

		// And do the same for all children
		Iterator<String> nodeTypeIterator = sourceData.getChildrenClassIterator();
		while (nodeTypeIterator.hasNext()) {
			String nodeTypeObject = nodeTypeIterator.next();

			Iterator<HierarchicalData> childDataIter = sourceData.getChildrenDataIterator(nodeTypeObject);
			while (childDataIter.hasNext()) {
				HierarchicalData childNode = childDataIter.next();
				cloneTreeIntoContainer(childNode, clone);
			}

		}
	}

	public HierarchicalData clonePotentiallyConflictingTreeTreeIntoContainer(@Nonnull HierarchicalData sourceData,
	                                                                         @Nonnull HierarchicalData targetParentNode) {
		validated = false;

		// Duplicate the node
		HierarchicalData clone = sourceData.duplicatePotentiallyConflictingDisconnectedClone(targetParentNode.node);
		clone.setParentContainer(this);
		clone.isRootNode = false;
		if (clone.getParent() != null) {
			clone.clearParentNode();
		}

		setParentNode(clone, targetParentNode);

		// And do the same for all children
		Iterator<String> nodeTypeIterator = sourceData.getChildrenClassIterator();
		while (nodeTypeIterator.hasNext()) {
			String nodeTypeObject = nodeTypeIterator.next();

			Iterator<HierarchicalData> childDataIter = sourceData.getChildrenDataIterator(nodeTypeObject);
			while (childDataIter.hasNext()) {
				HierarchicalData originalChildNode = childDataIter.next();
				clonePotentiallyConflictingTreeTreeIntoContainer(originalChildNode, clone);
			}
		}
		return clone;
	}

	public HierarchicalData cloneTrimmedTreeIntoContainer(@Nonnull HierarchicalData sourceData,
	                                                      @Nonnull HierarchicalData targetParentNode) {
		validated = false;

		// Duplicate the node
		HierarchicalData clone = sourceData.duplicateDisconnectedClone(targetParentNode.node);
		clone.setParentContainer(this);
		clone.isRootNode = false;
		if (clone.getParent() != null) {
			clone.clearParentNode();
		}

		setParentNode(clone, targetParentNode);

		// And do the same for all children
		Iterator<String> nodeTypeIterator = sourceData.getChildrenClassIterator();
		while (nodeTypeIterator.hasNext()) {
			String nodeTypeObject = nodeTypeIterator.next();

			Iterator<HierarchicalData> childDataIter = sourceData.getChildrenDataIterator(nodeTypeObject);
			while (childDataIter.hasNext()) {
				HierarchicalData originalChildNode = childDataIter.next();
				cloneTrimmedTreeIntoContainer(originalChildNode, clone);
			}
		}
		return clone;
	}

	@Override
	public HierarchicalDataContainer duplicate() {
		return duplicate(this.dataType);
	}

	/**
	 * Note: This duplicates everything except for the parentContainer since this duplicate will likely be placed
	 * into a new one!
	 */
	public HierarchicalDataContainer duplicate(@Nonnull DataType mode) {
		if (!validated) {
			validate();
		}
		LinkedHashMap<HierarchicalIdentifier, HierarchicalData> existingDataIdentifierMapClone =
				Utils.duplicateMap(existingDataIdentifierMap);

		LinkedHashMap<HierarchicalData, List<HierarchicalData>> superParentChildElementsMapClone = new LinkedHashMap<>();

		for (Map.Entry<HierarchicalData, List<HierarchicalData>> entry : superParentChildElements.entrySet()) {
			HierarchicalData parent = existingDataIdentifierMapClone.get(entry.getKey().node);
			LinkedList<HierarchicalData> children = new LinkedList<>();
			superParentChildElementsMapClone.put(parent, children);

			for (HierarchicalData child : entry.getValue()) {
				children.add(existingDataIdentifierMapClone.get(child.node));
			}
		}

		HierarchicalDataContainer rval = new HierarchicalDataContainer(
				mode,
				existingDataIdentifierMapClone,
				superParentChildElementsMapClone);
		rval.validate();
		return rval;
	}

	Set<HierarchicalData> getDataSet(@Nonnull Set<HierarchicalIdentifier> nodeSet) {
		Set<HierarchicalData> rval = new HashSet<>();

		for (HierarchicalIdentifier node : nodeSet) {
			rval.add(existingDataIdentifierMap.get(node));
		}

		return rval;
	}

	public Iterator<HierarchicalData> getDataIterator() {
		return existingDataIdentifierMap.values().iterator();
	}

	public Set<HierarchicalIdentifier> getNodeIdentifiers() {
		return existingDataIdentifierMap.keySet();
	}

	public HierarchicalData getNode(HierarchicalIdentifier identifier) {
		return existingDataIdentifierMap.get(identifier);
	}

	public Set<HierarchicalData> getDauRootNodes() {
		return superParentChildElements.keySet();
	}

	public Collection<HierarchicalData> getDauNodes(@Nonnull HierarchicalData dauNode) {
		return superParentChildElements.get(dauNode);

	}

	public boolean containsNode(HierarchicalIdentifier identifier) {
		return existingDataIdentifierMap.containsKey(identifier);
	}

	public void removeNodeTree(@Nonnull HierarchicalData node) {
		validated = false;
		if (!existingDataIdentifierMap.containsValue(node)) {
			throw AdaptationnException.internal("Could not find the node '" + node.toString() + "'!");
		}

		Set<HierarchicalData> removalList = new HashSet<>();

		Iterator<String> childTypeIter = node.getChildrenClassIterator();
		while (childTypeIter.hasNext()) {
			String childType = childTypeIter.next();

			Iterator<HierarchicalData> childIter = node.getChildrenDataIterator(childType);
			while (childIter.hasNext()) {
				HierarchicalData child = childIter.next();
				removalList.add(child);

			}
		}

		for (HierarchicalData nodeToRemove : removalList) {
			removeNodeTree(nodeToRemove);
		}
		removeNode(node);
	}

	public void removeNode(@Nonnull HierarchicalData node) {
		validated = false;
		HierarchicalData parent = node.getParentData();
		HierarchicalData rootNode = node.getRootNode();

		parent.removeChildNode(node.node, true);
		existingDataIdentifierMap.remove(node.node);
		superParentChildElements.get(rootNode).remove(node);
		node.clearParentNode();

		for (List<HierarchicalData> dauNodes : new HashSet<>(superParentChildElements.values())) {
			if (dauNodes.contains(node)) {
				throw AdaptationnException.internal("Multiple node trees should not contain the same node!");
			}
		}
	}

	private void setParentNode(@Nonnull HierarchicalData childNode, @Nonnull HierarchicalData parentNode) {
		validated = false;
		if (childNode.getParent() != null) {
			throw AdaptationnException.internal("Can only set parent node once!");
		}

		childNode.parentNode = parentNode.node;
		childNode.isRootNode = false;

		parentNode.addChildNode(childNode.node);
		if (parentNode.isRootNode()) {
			superParentChildElements.get(parentNode).add(childNode);
		} else {
			superParentChildElements.get(parentNode.getRootNode()).add(childNode);
		}
		existingDataIdentifierMap.put(childNode.node, childNode);
	}

	private void overrideParentNode(@Nonnull HierarchicalData childNode, @Nonnull HierarchicalData newParentNode) {
		validated = false;
		if (childNode.getParentData() == null) {
			throw AdaptationnException.internal("THe parent node has not been set!");
		}

		removeNode(childNode);
		setParentNode(childNode, newParentNode);
	}

	public void removeAndShortParentAndChildNodes(@Nonnull HierarchicalData childNode, Configuration.TransformationInstructions instructions, boolean preserveDebugData) {
		validated = false;
		HierarchicalData parentNode = childNode.getParentData();

		Set<HierarchicalData> children = new HashSet<>();

		Iterator<String> childTypeIter = childNode.getChildrenClassIterator();
		while (childTypeIter.hasNext()) {
			String childType = childTypeIter.next();

			Iterator<HierarchicalData> childIter = childNode.getChildrenDataIterator(childType);
			while (childIter.hasNext()) {
				HierarchicalData child = childIter.next();
				children.add(child);
			}
		}

		for (HierarchicalData child : children) {
			overrideParentNode(child, parentNode);
		}

		for (String key : new HashSet<>(childNode.getAttributeNames())) {
			Object value = childNode.getAttribute(key);

			if (parentNode.getAttribute(key) != null) {
				if (instructions.combineSquashedChildNodeAttributes.containsKey(parentNode.getNodeType()) &&
						instructions.combineSquashedChildNodeAttributes.get(parentNode.getNodeType()).containsKey(childNode.getNodeType()) &&
						instructions.combineSquashedChildNodeAttributes.get(parentNode.getNodeType()).get(childNode.getNodeType()).contains(key)) {
					Object existingValue = parentNode.getAttribute(key);
					if (existingValue instanceof Object[]) {
						Object[] existingArray = (Object[]) existingValue;
						Object[] newArray = new Object[existingArray.length + 1];
						System.arraycopy(existingArray, 0, newArray, 0, existingArray.length);
						newArray[existingArray.length] = value;
						parentNode.removeAttribute(key);
						parentNode.addAttribute(childNode.node, key, newArray);

					} else {
						if (!value.equals(existingValue)) {
							Object[] newArray = new Object[2];
							newArray[0] = existingValue;
							newArray[1] = value;
							parentNode.removeAttribute(key);
							parentNode.addAttribute(childNode.node, key, newArray);
						}
					}

				} else if (parentNode.getAttribute(key).equals(childNode.getAttribute(key))) {
					childNode.removeAttribute(key);

				} else {
					List<Configuration.ResolutionStrategy> resolutionStrategies = instructions.resolutionStrategies.stream().filter(x -> x.attributeLabel.equals(key)).collect(Collectors.toList());

					Optional<Configuration.ResolutionStrategySolution> solution = Optional.empty();

					for (Configuration.ResolutionStrategy strategy : resolutionStrategies) {
						if (parentNode.pathMatches(strategy.node1Path)) {
							if (childNode.pathMatches(strategy.node2Path)) {
								solution = strategy.resolutionStrategySolutions.stream().filter(
										x -> x.node1Value.equals(parentNode.getAttribute(key)) &&
												x.node2Values.contains(childNode.getAttribute(key).toString())).findFirst();

							}

						} else if (parentNode.pathMatches(strategy.node2Path)) {
							if (childNode.pathMatches(strategy.node1Path)) {
								solution = strategy.resolutionStrategySolutions.stream().filter(
										x -> x.node1Value.equals(childNode.getAttribute(key)) &&
												x.node2Values.contains(parentNode.getAttribute(key).toString())).findFirst();
							}
						}
					}

					// TODO: This should take into account the origin of the child originally, not just the current structure!
					if (solution.isPresent()) {
						parentNode.overrideAttribute(childNode.node, key, solution.get().result);
						childNode.removeAttribute(key);
					} else {
						throw AdaptationnException.internal("Multiple '" + parentNode.getNodeType() + "/" + childNode.getNodeType() + " '" + key + "' attributes found that are not flagged for combining!");
					}
				}
			} else {
				parentNode.addAttribute(childNode.node, key, value);
			}
		}

		if (childNode.getDebugData() != null && preserveDebugData) {
			throw new RuntimeException("Child debug data is not yet supported!");
		}

		removeNode(childNode);

		Iterator<String> childNodeTypeIter = parentNode.getChildrenClassIterator();
		while (childNodeTypeIter.hasNext()) {
			String nodeType = childNodeTypeIter.next();
			parentNode.removeAttribute(nodeType);
		}
	}

	public Set<HierarchicalData> getNodesAtPath(@Nonnull List<String> path) {
		List<String> pathList = new ArrayList<>(path);
		String head = pathList.remove(0);
		Set<HierarchicalData> dataSet = existingDataIdentifierMap.values().stream().filter(x -> x.getNodeType().equals(head)).collect(Collectors.toSet());
		dataSet.addAll(
				superParentChildElements.keySet().stream().filter(x -> x.getNodeType().equals(head)).collect(Collectors.toSet())
		);

		for (String nextValue : pathList) {
			Set<HierarchicalData> children = new HashSet<>();

			for (HierarchicalData parentData : dataSet) {
				Iterator<HierarchicalData> childIter = parentData.getChildrenDataIterator(nextValue);
				while (childIter.hasNext()) {
					children.add(childIter.next());
				}
			}
			dataSet = children;
		}
		return dataSet;
	}

}
