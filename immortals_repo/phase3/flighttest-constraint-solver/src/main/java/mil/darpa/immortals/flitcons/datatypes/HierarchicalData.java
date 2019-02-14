package mil.darpa.immortals.flitcons.datatypes;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HierarchicalData {
	public final boolean isRootNode;

	private final Set<HierarchicalNode> inboundReferences;
	private final Set<HierarchicalNode> outboundReferences;

	public final HierarchicalNode node;

	private static final String INSERTION_ATTRIBUTE_LABEL = "BBNIDTAG";

	public String getInsertionAttributeLabel() {
		return INSERTION_ATTRIBUTE_LABEL;
	}

	public final String sourceIdentifier;

	private static AtomicInteger idCounter = new AtomicInteger(0);

	public final String nodeClass;

	private final Map<String, Object> attributes;
	private HierarchicalData parentNode;

	private final Map<String, Set<HierarchicalData>> childNodeMap = new HashMap<>();

	public final Object associatedObject;

	public String getIdentifier() {
		String rval = (sourceIdentifier == null ? "" : (sourceIdentifier + " "));

		if (attributes.containsKey("Manufacturer") && attributes.containsKey("Model")) {
			rval = rval + "(Manufacturer=\"" + attributes.get("Manufacturer") + "\", Model=\"" + attributes.get("Model") + "\")";
		}
		return rval;
	}

	public List<String> getPath() {
		List<String> rval = new LinkedList<>(parentNode.getPath());
		rval.add(getIdentifier());
		return rval;
	}

	public List<HierarchicalData> getPathAsData() {
		List<HierarchicalData> rval;
		if (parentNode == null) {
			if (!isRootNode) {
				throw new RuntimeException("A non-root node must have a parent node!");
			}
			rval = new LinkedList<>();
		} else {
			rval = new LinkedList<>(parentNode.getPathAsData());
			rval.add(parentNode);
		}
//		rval.add(this);
		return rval;
	}

	public String toString() {
		return "v(" + node.nodeType + ")[" + sourceIdentifier + "]";
	}

	public HierarchicalData(@Nonnull String nodeClass, @Nonnull Map<String, Object> attributes, @Nonnull String sourceIdentifier, @Nonnull Object associatedObject, boolean isRootNode) {
		this.nodeClass = nodeClass;
		this.attributes = new HashMap<>(attributes);
//		this.attributes.put(getInsertionAttributeLabel(), idCounter.getAndIncrement());
		this.sourceIdentifier = sourceIdentifier;
		this.associatedObject = associatedObject;
		this.node = new HierarchicalNode(sourceIdentifier, nodeClass);
		this.inboundReferences = new HashSet<>();
		this.outboundReferences = new HashSet<>();
		this.isRootNode = isRootNode;
	}

	public void setParentNode(HierarchicalData parentNode) {
		if (this.parentNode != null) {
			throw new RuntimeException("Can only set parent node once!");
		}
		this.parentNode = parentNode;

		Set<HierarchicalData> parentChildren = parentNode.childNodeMap.computeIfAbsent(nodeClass, k -> new HashSet<>());
		parentChildren.add(this);
	}

	public void overrideParentNode(HierarchicalData parentNode) {
		if (this.parentNode == null) {
			throw new RuntimeException("The parent node has not been set!");
		}

		this.parentNode.childNodeMap.get(this.nodeClass).remove(this);

		this.parentNode = parentNode;
		Set<HierarchicalData> parentChildren = parentNode.childNodeMap.computeIfAbsent(nodeClass, k -> new HashSet<>());
		parentChildren.add(this);
	}

	public String getNodeClass() {
		return nodeClass;
	}

	public HierarchicalData getParentNode() {
		return parentNode;
	}

	public Map<String, Set<HierarchicalData>> getChildNodeMap() {
		return childNodeMap;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}


	public Set<HierarchicalNode> getInboundReferencesSet() {
		return inboundReferences;
	}

	public Set<HierarchicalNode> getOutboundReferencesSet() {
		return outboundReferences;
	}
}