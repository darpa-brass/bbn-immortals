package mil.darpa.immortals.flitcons.datatypes.hierarchical;

import mil.darpa.immortals.flitcons.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

	private final Map<String, HierarchicalData> foreignAttributeSources;

	private final Map<String, Object> debugAttributes;

	HierarchicalIdentifier parentNode;

	private final Map<String, Set<HierarchicalIdentifier>> childNodeMap;

	private final Object associatedObject;

	private HierarchicalDataContainer parentContainer;

	public boolean isRootNode() {
		return isRootNode;
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
				Utils.duplicateMap(debugAttributes),
				Utils.duplicateMap(foreignAttributeSources)
		);
	}

	void setParentContainer(HierarchicalDataContainer parentContainer) {
		this.parentContainer = parentContainer;
	}

	void clearParentNode() {
		if (parentNode == null) {
			throw new RuntimeException("The parent node is not set!");
		}
		parentNode = null;
	}

	public List<HierarchicalData> getPathAsData() {
		List<HierarchicalData> rval;
		if (parentNode == null) {
			if (!isRootNode) {
				throw new RuntimeException("A non-root node must have a parent node!");
			}
			rval = new LinkedList<>();
		} else {
			HierarchicalData parentData = parentContainer.getData(parentNode);
			rval = new LinkedList<>(parentData.getPathAsData());
			rval.add(parentData);
		}
		return rval;
	}

	public String toString() {
		return node.toString();
	}

	public HierarchicalData(@Nonnull HierarchicalIdentifier identifier, @Nonnull Map<String, Object> attributes,
	                        @Nonnull Object associatedObject, boolean isRootNode,
	                        @Nullable HierarchicalIdentifier parent,
	                        @Nullable Set<HierarchicalIdentifier> inboundReferences,
	                        @Nullable Set<HierarchicalIdentifier> outboundReferences,
	                        @Nullable Map<String, Set<HierarchicalIdentifier>> childNodeMap,
	                        @Nullable Map<String, Object> debugAttributes) {
		this.attributes = attributes;
		this.associatedObject = associatedObject;
		this.node = identifier;
		this.isRootNode = isRootNode;
		this.parentNode = parent;
		this.inboundReferences = inboundReferences == null ? new HashSet<>() : inboundReferences;
		this.outboundReferences = outboundReferences == null ? new HashSet<>() : outboundReferences;
		this.childNodeMap = childNodeMap == null ? new HashMap<>() : childNodeMap;
		this.debugAttributes = debugAttributes == null ? new HashMap<>() : debugAttributes;
		this.foreignAttributeSources = new HashMap<>();
		validate();
	}

	private HierarchicalData(@Nonnull HierarchicalIdentifier identifier, @Nonnull Map<String, Object> attributes,
	                         @Nonnull Object associatedObject, boolean isRootNode,
	                         @Nullable HierarchicalIdentifier parent,
	                         @Nullable Set<HierarchicalIdentifier> inboundReferences,
	                         @Nullable Set<HierarchicalIdentifier> outboundReferences,
	                         @Nullable Map<String, Set<HierarchicalIdentifier>> childNodeMap,
	                         @Nullable Map<String, Object> debugAttributes,
	                         @Nullable Map<String, HierarchicalData> foreignAttributeSources) {
		this.attributes = attributes;
		this.associatedObject = associatedObject;
		this.node = identifier;
		this.isRootNode = isRootNode;
		this.parentNode = parent;
		this.inboundReferences = inboundReferences == null ? new HashSet<>() : inboundReferences;
		this.outboundReferences = outboundReferences == null ? new HashSet<>() : outboundReferences;
		this.childNodeMap = childNodeMap == null ? new HashMap<>() : childNodeMap;
		this.debugAttributes = debugAttributes == null ? new HashMap<>() : debugAttributes;
		this.foreignAttributeSources = foreignAttributeSources == null ? new HashMap<>() : foreignAttributeSources;
		validate();
	}

	private void validate() {
		if (isRootNode && parentNode != null) {
			throw new RuntimeException("A node cannot be a parent node and contain a parent node!");
		}

		if (!isRootNode && parentNode == null) {
			throw new RuntimeException("A Node must have a parent node if it is not a parent node!");
		}
	}

	public HierarchicalData getRootNode() {
		return getPathAsData().get(0);
	}

	void addChildNode(@Nonnull HierarchicalIdentifier childIdentifier) {
		childNodeMap.computeIfAbsent(childIdentifier.getNodeType(), k -> new HashSet<>()).add(childIdentifier);
	}

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

	public Iterator<HierarchicalData> getChildrenDataIterator(@Nonnull String nodeType) {
		Set<HierarchicalIdentifier> nodes = childNodeMap.get(nodeType);
		if (nodes == null) {
			return Collections.emptyIterator();
		} else {
			return parentContainer.getDataSet(nodes).iterator();
		}
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

	public Object getDebugAttribute(@Nonnull String key) {
		return debugAttributes.get(key);
	}

	public Map<String, Object> getDebugAttributes() {
		return debugAttributes;
	}

	Iterator<HierarchicalIdentifier> getInboundReferencesIterator() {
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
			throw new RuntimeException("Data contains no outbound references with ID '" + fullOutboundReference.referenceIdentifier + "'!");
		}
	}

	public String getIdentifier() {
		return node.getIdentifier();
	}

	void removeChildNode(HierarchicalIdentifier identifier, boolean removeTypeIfEmpty) {
		try {
			Set<HierarchicalIdentifier> typeMap = childNodeMap.get(identifier.getNodeType());
			typeMap.remove(identifier);

			if (removeTypeIfEmpty && typeMap.isEmpty()) {
				childNodeMap.remove(identifier.getNodeType());
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	void addAttributes(@Nonnull HierarchicalData source) {
		for (String key : source.attributes.keySet()) {
			this.attributes.put(key, source.attributes.get(key));
			this.foreignAttributeSources.put(key, source);
		}
	}

	void addDebugAttributes(@Nonnull HierarchicalData source) {
		for (String key : source.debugAttributes.keySet()) {
			this.debugAttributes.put(key, source.debugAttributes.get(key));
		}
	}

	String setTag() {
		String tag = Integer.toHexString(tagCounter.incrementAndGet());
		attributes.put("GloballyUniqueId", "Ox" + tag);
		return tag;
	}

	public Set<String> getAttributeNames() {
		return attributes.keySet();
	}
}