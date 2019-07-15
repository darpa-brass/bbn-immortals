package mil.darpa.immortals.flitcons.datatypes.hierarchical;

import mil.darpa.immortals.flitcons.Utils;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * A data format meant to act as an intermediary that is not XML or OrientDB but conveys the same necessary details
 */
public class HierarchicalData implements DuplicateInterface {

	private static AtomicInteger tagCounter = new AtomicInteger(1248576);

	boolean isRootNode;

	private final Set<HierarchicalIdentifier> inboundReferences;
	private final Set<HierarchicalIdentifier> outboundReferences;

	public final HierarchicalIdentifier node;

	private final Map<String, Object> attributes;

	private final Map<String, HierarchicalIdentifier> foreignAttributeSources;

	private String debugLabel;

	HierarchicalIdentifier parentNode;

	private final Map<String, Set<HierarchicalIdentifier>> childNodeMap;

	public final Object associatedObject;

	private HierarchicalDataContainer parentContainer;

	public boolean isRootNode() {
		return isRootNode;
	}

	public HierarchicalIdentifier getAttributeParent(@Nonnull String attributeName) {
		return foreignAttributeSources.getOrDefault(attributeName, this.node);
	}

	public void updateDebugLabel(@Nonnull String labelAddendum) {
		if (debugLabel == null) {
			debugLabel = labelAddendum;
		} else {
			debugLabel += "|" + labelAddendum;
		}
	}

	@Nonnull
	public String getDebugLabel() {
		return debugLabel;
	}

	public HierarchicalData duplicateWithNewNode() {
			return new HierarchicalData(
				HierarchicalIdentifier.produceTraceableNode(UUID.randomUUID().toString(), node.getNodeType()),
				Utils.duplicateMap(attributes),
				associatedObject,
				isRootNode,
				parentNode,
				Utils.duplicateSet(inboundReferences),
				Utils.duplicateSet(outboundReferences),
				Utils.duplicateSetMap(childNodeMap),
				debugLabel,
				Utils.duplicateMap(foreignAttributeSources)
		);
	}

	@Override
	public HierarchicalData duplicate() {
		return new HierarchicalData(
				node,
				Utils.duplicateMap(attributes),
				associatedObject,
				isRootNode,
				parentNode,
				Utils.duplicateSet(inboundReferences),
				Utils.duplicateSet(outboundReferences),
				Utils.duplicateSetMap(childNodeMap),
				debugLabel,
				Utils.duplicateMap(foreignAttributeSources)
		);
	}

	public HierarchicalData duplicateFlatDisconnected(@Nullable HierarchicalIdentifier targetParentNode) {
		return new HierarchicalData(
				HierarchicalIdentifier.produceTraceableNode(UUID.randomUUID().toString(), node.getNodeType()),
				Utils.duplicateMap(attributes),
				new Object(),
				false,
				targetParentNode,
				null,
				null,
				null,
				null
		);
	}

	public void setParentContainer(HierarchicalDataContainer parentContainer) {
		this.parentContainer = parentContainer;
	}

	void clearParentNode() {
		if (parentNode == null) {
			throw AdaptationnException.internal("The parent node is not set!");
		}
		parentNode = null;
	}


	public boolean pathMatches(@Nonnull LinkedList<String> testPath) {

		if (!testPath.getLast().equals(getNodeType())) {
			return false;
		}

		List<String> myPath = getPathAsData().stream().map(HierarchicalData::getNodeType).collect(Collectors.toList());
		int myPathIdx = myPath.size() - 1;
		int testPathIdx = testPath.size() - 2;
		boolean doesMatch = true;

		while (doesMatch && myPathIdx >= 0 && testPathIdx >= 0) {
			if (myPath.get(myPathIdx).equals(testPath.get(testPathIdx))) {
				myPathIdx--;
				testPathIdx--;
				if (myPathIdx < 0 && testPathIdx >= 0) {
					doesMatch = false;
				}
			} else {
				doesMatch = false;
			}
		}
		return doesMatch;
	}

	@Nonnull
	public List<HierarchicalData> getPathAsData() {
		List<HierarchicalData> rval;
		if (parentNode == null) {
			if (!isRootNode) {
				throw AdaptationnException.internal("A non-root node must have a parent node!");
			}
			rval = new LinkedList<>();
		} else {
			HierarchicalData parentData = parentContainer.getData(parentNode);
			rval = new LinkedList<>(parentData.getPathAsData());
			rval.add(parentData);
		}
		return rval;
	}

	@Nonnull
	public String toString() {
		return node.toString();
	}

	public HierarchicalData(@Nonnull HierarchicalIdentifier identifier, @Nonnull Map<String, Object> attributes,
	                        @Nonnull Object associatedObject, boolean isRootNode,
	                        @Nullable HierarchicalIdentifier parent,
	                        @Nullable Set<HierarchicalIdentifier> inboundReferences,
	                        @Nullable Set<HierarchicalIdentifier> outboundReferences,
	                        @Nullable Map<String, Set<HierarchicalIdentifier>> childNodeMap,
	                        @Nullable String debugLabel) {
		this.attributes = attributes;
		this.associatedObject = associatedObject;
		this.node = identifier;
		this.isRootNode = isRootNode;
		this.parentNode = parent;
		this.inboundReferences = inboundReferences == null ? new HashSet<>() : inboundReferences;
		this.outboundReferences = outboundReferences == null ? new HashSet<>() : outboundReferences;
		this.childNodeMap = childNodeMap == null ? new HashMap<>() : childNodeMap;
		this.debugLabel = debugLabel;
		this.foreignAttributeSources = new HashMap<>();
		validate();
	}

	private HierarchicalData(@Nonnull HierarchicalIdentifier identifier, @Nonnull Map<String, Object> attributes,
	                         @Nonnull Object associatedObject, boolean isRootNode,
	                         @Nullable HierarchicalIdentifier parent,
	                         @Nullable Set<HierarchicalIdentifier> inboundReferences,
	                         @Nullable Set<HierarchicalIdentifier> outboundReferences,
	                         @Nullable Map<String, Set<HierarchicalIdentifier>> childNodeMap,
	                         @Nullable String debugLabel,
	                         @Nullable Map<String, HierarchicalIdentifier> foreignAttributeSources) {
		this.attributes = attributes;
		this.associatedObject = associatedObject;
		this.node = identifier;
		this.isRootNode = isRootNode;
		this.parentNode = parent;
		this.inboundReferences = inboundReferences == null ? new HashSet<>() : inboundReferences;
		this.outboundReferences = outboundReferences == null ? new HashSet<>() : outboundReferences;
		this.childNodeMap = childNodeMap == null ? new HashMap<>() : childNodeMap;
		this.debugLabel = debugLabel;
		this.foreignAttributeSources = foreignAttributeSources == null ? new HashMap<>() : foreignAttributeSources;
		validate();
	}

	private void validate() {
		if (isRootNode && parentNode != null) {
			throw AdaptationnException.internal("A node cannot be a parent node and contain a parent node!");
		}

		if (!isRootNode && parentNode == null) {
			throw AdaptationnException.internal("A Node must have a parent node if it is not a parent node!");
		}
	}

	@Nonnull
	public HierarchicalData getRootNode() {
		return getPathAsData().get(0);
	}

	void addChildNode(@Nonnull HierarchicalIdentifier childIdentifier) {
		childNodeMap.computeIfAbsent(childIdentifier.getNodeType(), k -> new HashSet<>()).add(childIdentifier);
	}

	@Nonnull
	public String getNodeType() {
		return node.getNodeType();
	}

	public HierarchicalData getParentData() {
		return parentContainer.getData(parentNode);
	}

	public HierarchicalIdentifier getParent() {
		return parentNode;
	}

	public Iterator<String> getChildrenClassIterator() {
		return childNodeMap.keySet().iterator();
	}

	@Nonnull
	public Set<String> getCommonAttributes(@Nonnull Set<String> attributes) {
		HashSet<String> clone = new HashSet<>(attributes);
		clone.retainAll(attributes);
		return clone;
	}

	public Iterator<HierarchicalData> getChildrenDataIterator(@Nonnull String nodeType) {
		Set<HierarchicalIdentifier> nodes = childNodeMap.get(nodeType);
		if (nodes == null) {
			return Collections.emptyIterator();
		} else {
			return parentContainer.getDataSet(nodes).iterator();
		}
	}

	@Nullable
	public HierarchicalData getChildNodeByPath(@Nonnull List<String> path) {
		Set<HierarchicalData> children = getChildNodesByPath(path);
		if (children.size() == 0) {
			return null;
		} else if (children.size() > 1) {
			throw AdaptationnException.input("Cannot determine proper child to select with multiple children!");
		}
		return children.iterator().next();
	}

	public Set<HierarchicalData> getChildNodesByPath(@Nonnull List<String> path) {
		HierarchicalData currentNode = this;

		Iterator<String> pathIter = path.iterator();
		while (pathIter.hasNext()) {
			String pathElement = pathIter.next();

			Set<HierarchicalIdentifier> children = currentNode.childNodeMap.get(pathElement);

			if (pathIter.hasNext()) {
				if (children == null || children.size() == 0) {
					return Collections.emptySet();
				} else if (children.size() > 1) {
					throw AdaptationnException.internal("Collection of child nodes when branches exist in the structure is not currently supported!");
				}
				currentNode = parentContainer.getData(children.iterator().next());
			} else if (children == null) {
				return Collections.emptySet();
			} else {
				return parentContainer.getDataSet(children);
			}
		}
		return new HashSet<>();
	}

	void removeAttribute(@Nonnull String attributeName) {
		attributes.remove(attributeName);
	}

	@Deprecated
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public Object getAttribute(@Nonnull String key) {
		return attributes.get(key);
	}

	public Iterator<HierarchicalIdentifier> getInboundReferencesIterator() {
		return inboundReferences.iterator();
	}

	public Iterator<HierarchicalIdentifier> getOutboundReferencesIterator() {
		return outboundReferences.iterator();
	}


	public void updateOutboundReference(@Nonnull HierarchicalIdentifier fullOutboundReference) {
		Optional<HierarchicalIdentifier> currentIdentifier =
				outboundReferences.stream().filter(
						x -> x.referenceIdentifier.equals(fullOutboundReference.referenceIdentifier)).findFirst();

		if (currentIdentifier.isPresent()) {
			outboundReferences.remove(currentIdentifier.get());
			outboundReferences.add(fullOutboundReference);
		} else {
			throw AdaptationnException.internal("Data contains no outbound references with ID '" + fullOutboundReference.referenceIdentifier + "'!");
		}
	}

	void removeChildNode(HierarchicalIdentifier identifier, boolean removeTypeIfEmpty) {
		Set<HierarchicalIdentifier> typeMap = childNodeMap.get(identifier.getNodeType());
		typeMap.remove(identifier);

		if (removeTypeIfEmpty && typeMap.isEmpty()) {
			childNodeMap.remove(identifier.getNodeType());
		}
	}

	void addAttribute(@Nonnull HierarchicalIdentifier source, @Nonnull String attributeName, @Nonnull Object attributeValue) {
		this.attributes.put(attributeName, attributeValue);
		this.foreignAttributeSources.put(attributeName, source);
	}

	String exposeTag() {
		String tag = node.getUniqueSessionIdentifier();
		attributes.put("GloballyUniqueId", tag);
		return tag;
	}

	public Set<String> getAttributeNames() {
		return attributes.keySet();
	}
}
