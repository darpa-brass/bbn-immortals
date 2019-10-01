package mil.darpa.immortals.flitcons.datatypes.hierarchical;


import mil.darpa.immortals.flitcons.reporting.AdaptationnException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The intent of this object is to facilitate a comparable immutable standard identifier for nodes.
 * The identifier and nodeType value are used as a pair to strictly identify the node in most circumstances
 * <p>
 * Since XML isn't aware of the relationship between the ID and IDREF fields of MDL the  referenceIdentifier is
 * also supplied as a shortcut for gathering references that can be properly associated after {@link HierarchicalData}
 * collection.
 */
@Immutable
public class HierarchicalIdentifier implements Comparator<HierarchicalIdentifier>, Comparable<HierarchicalIdentifier> {

	private static Set<HierarchicalIdentifier> identifierDatastore = new HashSet<>();

	private static AtomicInteger tagCounter = new AtomicInteger(1248576);

	private static AtomicInteger cloneCounter = new AtomicInteger(0);

	/**
	 * An identifier that can be used to match this with the corresponding node in the original data source
	 */
	private String sourceIdentifier;

	/**
	 * An identifier that provides a concrete reference for this node to be used globally in this session
	 */
	private final String uniqueSessionIdentifier;

	/**
	 * The type of the node
	 */
	private String nodeType;

	/**
	 * The reference identifier for this node, if any
	 */
	public String referenceIdentifier;

	public String getSourceIdentifier() {
		if (sourceIdentifier == null) {
			throw AdaptationnException.internal("No source identifier exists for this object!");
		}
		return sourceIdentifier;
	}

	public String getUniqueSessionIdentifier() {
		return uniqueSessionIdentifier;
	}

	public String getNodeType() {
		if (nodeType == null) {
			throw AdaptationnException.internal("NodeType not specified!");
		}
		return nodeType;
	}

	public static HierarchicalIdentifier createBlankNode() {
		return new HierarchicalIdentifier(null, null, null);
	}

	public static HierarchicalIdentifier createReferenceNode(@Nonnull String referenceIdentifier, @Nullable String nodeType) {
		Optional<HierarchicalIdentifier> candidate = identifierDatastore.stream().filter(x ->
				referenceIdentifier.equals(x.referenceIdentifier) && (nodeType == null || x.nodeType == null || nodeType.equals(x.nodeType))).findFirst();

		if (candidate.isPresent()) {
			HierarchicalIdentifier val = candidate.get();
			if (val.nodeType == null) {
				val.nodeType = nodeType;
			}
			return val;
		} else {
			return new HierarchicalIdentifier(null, nodeType, referenceIdentifier);
		}
	}


	public static HierarchicalIdentifier produceTraceableNode(@Nonnull String identifier, @Nullable String nodeType) {
		Optional<HierarchicalIdentifier> candidate = identifierDatastore.stream().filter(x ->
				identifier.equals(x.sourceIdentifier) && (nodeType == null || x.nodeType == null || nodeType.equals(x.nodeType))).findFirst();

		if (candidate.isPresent()) {
			HierarchicalIdentifier val = candidate.get();
			if (val.nodeType == null) {
				val.nodeType = nodeType;
			}
			return val;
		} else {
			return new HierarchicalIdentifier(identifier, nodeType, null);
		}
	}

	public HierarchicalIdentifier createIdentitylessClone() {
		return new HierarchicalIdentifier(sourceIdentifier + "-d3715d0d-" + cloneCounter.getAndAdd(1), nodeType, null);
	}

	public static HierarchicalIdentifier getByStringIdentifier(@Nonnull String identifier) {
		Optional<HierarchicalIdentifier> candidate = identifierDatastore.stream().filter(x ->
				identifier.equals(x.sourceIdentifier)).findFirst();
		if (candidate.isPresent()) {
			return candidate.get();
		} else {
			throw AdaptationnException.internal("Could not find node with identifier '" + identifier + "'!");

		}

	}

	private HierarchicalIdentifier(@Nullable String identifier, @Nullable String nodeType, @Nullable String referenceIdentifier) {
		this.sourceIdentifier = identifier;
		this.uniqueSessionIdentifier = identifier == null ? ("I" + Integer.toHexString(tagCounter.incrementAndGet())) : identifier;
		this.nodeType = nodeType;
		this.referenceIdentifier = referenceIdentifier;
		identifierDatastore.add(this);
	}

	@Override
	public int compareTo(@Nonnull HierarchicalIdentifier hierarchicalIdentifier) {
		return compare(this, hierarchicalIdentifier);
	}

	@Override
	public int compare(HierarchicalIdentifier o, HierarchicalIdentifier t1) {
		if (o == null) {
			if (t1 == null) {
				return 0;
			} else {
				return -1;
			}
		} else {
			if (t1 == null) {
				return 1;
			} else {
				if (o.getUniqueSessionIdentifier().equals(t1.getUniqueSessionIdentifier())) {
					return 0;

				} else if (o.sourceIdentifier != null && o.sourceIdentifier.equals(t1.sourceIdentifier)) {
					if (o.getNodeType().equals(t1.getNodeType()) || o.getNodeType().equals(t1.getNodeType())) {
						return 0;
					} else {
						throw AdaptationnException.internal("Comparison of node with identifier '" + o.toString() + "' has indicated multiple node types of '" + o.nodeType + "' and '" + t1.nodeType + "'!");
					}

				} else {
					return -1;
				}
			}
		}
	}

	@Override
	public int hashCode() {
		return (getUniqueSessionIdentifier()).hashCode();
	}

	public String toString() {
		String nodeTypeStr = nodeType == null ? "?" : nodeType;
		String identifierStr = sourceIdentifier == null ? "?" : sourceIdentifier;
		String rval = "v(" + nodeTypeStr + ")[" + identifierStr + "]{uniqueSessionIdentifier=" + uniqueSessionIdentifier;
		if (referenceIdentifier == null) {
			rval = rval + "}";
		} else {
			rval = rval + ",ID=" + referenceIdentifier + "}";
		}
		return rval;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof HierarchicalIdentifier)) {
			return false;
		} else {
			return compare(this, (HierarchicalIdentifier) o) == 0;
		}
	}
}
