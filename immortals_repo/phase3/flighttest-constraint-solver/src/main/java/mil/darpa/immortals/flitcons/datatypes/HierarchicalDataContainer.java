package mil.darpa.immortals.flitcons.datatypes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class HierarchicalDataContainer<T> implements DuplicateInterface<HierarchicalDataContainer> {

	protected final Map<String, Set<String>> interestedDauPropertes;
	protected final Map<String, HierarchicalData> existingDataIdentifierMap = new HashMap<>();

	protected final Map<HierarchicalData, Set<HierarchicalData>> superParentChildElements = new HashMap<>();

	public HierarchicalDataContainer(@Nonnull Collection<T> dauNodes, @Nonnull Map<String, Set<String>> interestedDauPropertes) {
		this.interestedDauPropertes = interestedDauPropertes;

		for (T ov : dauNodes) {
			addOrGetDataInternal(ov, null, true);
		}
	}

	protected HierarchicalDataContainer(@Nonnull Map<String, Set<String>> interestedDauPropertes) {
		this.interestedDauPropertes = interestedDauPropertes;
	}

	protected static void checkState(HierarchicalDataContainer sourceGraph) {

		Set<HierarchicalData> allChildNodes = new HashSet<>();

		for (Object superParentObject : sourceGraph.superParentChildElements.keySet()) {
			HierarchicalData superParent = (HierarchicalData) superParentObject;
			allChildNodes.add(superParent);

			Set<HierarchicalData> childNodes = (Set<HierarchicalData>) sourceGraph.superParentChildElements.get(superParentObject);

			for (HierarchicalData src : childNodes) {
				allChildNodes.add(src);
				if (!sourceGraph.existingDataIdentifierMap.containsKey(src.sourceIdentifier)) {
					throw new RuntimeException("Matching key not found for node associated with root node!");
				} else if (!sourceGraph.existingDataIdentifierMap.containsValue(src)) {
					throw new RuntimeException("Matching object not found for node associated with root node!");
				}
			}
		}

		for (Object keyObject : sourceGraph.existingDataIdentifierMap.keySet()) {
			String key = (String) keyObject;
			HierarchicalData value = (HierarchicalData) sourceGraph.existingDataIdentifierMap.get(key);

			if (!allChildNodes.contains(value)) {
				throw new RuntimeException("Not all nodes contained in the main node map have an identified root node!");
			}
		}
	}

	private HierarchicalData createAndInsertClone(@Nullable HierarchicalData rootNode, @Nonnull HierarchicalData src, boolean isRootNode) {

		if (src == null) {
			System.out.println("MEH");
		}

		if (existingDataIdentifierMap.values().contains(src)) {
			throw new RuntimeException("Cannot insert and clone a node from the same database!");

		} else if (existingDataIdentifierMap.containsKey(src.sourceIdentifier)) {
			return existingDataIdentifierMap.get(src.getIdentifier());

		} else {
			HierarchicalData clone = new HierarchicalData(
					src.nodeClass,
					src.getAttributes(),
					src.sourceIdentifier,
					src.associatedObject,
					isRootNode
			);

			clone.getInboundReferencesSet().addAll(src.getInboundReferencesSet());
			clone.getOutboundReferencesSet().addAll(src.getOutboundReferencesSet());

			existingDataIdentifierMap.put(clone.sourceIdentifier, clone);

			if (isRootNode) {
				if (rootNode == null) {
					rootNode = clone;

					if (!superParentChildElements.containsKey(clone.sourceIdentifier)) {
						Set<HierarchicalData> childrenNodes = new HashSet<>();
						childrenNodes.add(clone);
						superParentChildElements.put(clone, childrenNodes);
					}


				} else {
					throw new RuntimeException("Root node value should not be supplied for a root node!!");
				}

			} else {
				if (rootNode == null) {
					throw new RuntimeException("Root node not supplied!");
				} else {
					clone.setParentNode(existingDataIdentifierMap.get(src.getParentNode().sourceIdentifier));
					superParentChildElements.get(rootNode).add(clone);
				}
			}

			for (String nodeTypeObject : src.getChildNodeMap().keySet()) {
				Set<HierarchicalData> targetSet = clone.getChildNodeMap().computeIfAbsent(nodeTypeObject, k -> new HashSet<>());

				for (HierarchicalData childNode : src.getChildNodeMap().get(nodeTypeObject)) {
					HierarchicalData childClone = createAndInsertClone(rootNode, childNode, false);
					targetSet.add(childClone);
				}

			}
			return clone;
		}
	}

	protected static void duplicate(HierarchicalDataContainer sourceGraph, HierarchicalDataContainer targetGraph) {
		checkState(sourceGraph);

		for (Object rootNodeObject : sourceGraph.superParentChildElements.keySet()) {
			HierarchicalData rootNode = (HierarchicalData) rootNodeObject;
			HierarchicalData rootNodeClone = targetGraph.createAndInsertClone(null, rootNode, true);

			Set<HierarchicalData> childNodes = (Set<HierarchicalData>) sourceGraph.superParentChildElements.get(rootNodeObject);

			for (HierarchicalData src : childNodes) {
				targetGraph.createAndInsertClone(rootNodeClone, src, false);
			}
		}
	}

	protected abstract HierarchicalData addOrGetDataInternal(T v, @Nullable List<T> path, boolean isRootObject);

	public abstract HierarchicalDataContainer duplicate();

	public Set<String> getNodeIdentifiers() {
		return existingDataIdentifierMap.keySet();
	}

	public HierarchicalData getNode(@Nonnull String identifier) {
		return existingDataIdentifierMap.get(identifier);
	}

	public Collection<HierarchicalData> getDauRootNodes() {
		return superParentChildElements.keySet();
	}

	public Collection<HierarchicalData> getDauNodes(@Nonnull HierarchicalData dauNode) {
		return superParentChildElements.get(dauNode);

	}

	public boolean containsNodeWithIdentifier(String identifier) {
		return existingDataIdentifierMap.containsKey(identifier);
	}

	public HierarchicalData addOrGetData(@Nonnull T v, @Nonnull List<T> path) {
		return addOrGetDataInternal(v, path, false);
	}

	public void removeNodeTree(@Nonnull HierarchicalData node) {
		if (!existingDataIdentifierMap.values().contains(node)) {
			throw new RuntimeException("Could not find the node '" + node.toString() + "'!");
		}

		for (Set<HierarchicalData> children : new HashSet<>(node.getChildNodeMap().values())) {
			for (HierarchicalData child : new HashSet<>(children)) {
				removeNodeTree(child);
			}
		}
		existingDataIdentifierMap.get(node.sourceIdentifier).getParentNode().getChildNodeMap().get(node.nodeClass).remove(node);
		existingDataIdentifierMap.remove(node.sourceIdentifier);
		for (Set<HierarchicalData> dauNodes : new HashSet<>(superParentChildElements.values())) {
			dauNodes.remove(node);
		}
	}

	public void removeAndShortParentAndChildNodes(@Nonnull HierarchicalData node) {
		List<HierarchicalData> path = node.getPathAsData();
		HierarchicalData parentNode = path.get(path.size() - 1);

		Set<Set<HierarchicalData>> originalEntrySet = new HashSet<>(node.getChildNodeMap().values());
		for (Set<HierarchicalData> entrySet : originalEntrySet) {
			for (HierarchicalData child : new HashSet<>(entrySet)) {
				child.overrideParentNode(parentNode);
			}
		}

		existingDataIdentifierMap.get(node.sourceIdentifier).getParentNode().getChildNodeMap().get(node.nodeClass).remove(node);
		existingDataIdentifierMap.remove(node.sourceIdentifier);
		for (Set<HierarchicalData> dauNodes : superParentChildElements.values()) {
			dauNodes.remove(node);
		}

	}
}
