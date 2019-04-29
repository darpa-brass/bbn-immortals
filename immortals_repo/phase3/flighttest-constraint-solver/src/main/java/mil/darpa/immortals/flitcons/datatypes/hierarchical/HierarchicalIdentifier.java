package mil.darpa.immortals.flitcons.datatypes.hierarchical;


import javax.annotation.Nonnull;
import java.util.Comparator;

/**
 * The intent of this object is to facilitate a comparable immutable standard identifier for nodes.
 * The identifier and nodeType value are used as a pair to strictly identify the node in most circumstances
 * <p>
 * Since XML isn't aware of the relationship between the ID and IDREF fields of MDL the  referenceIdentifier is
 * also supplied as a shortcut for gathering references that can be properly associated after {@Link HierarcicalData}
 * collection.
 */
@Immutable
public class HierarchicalIdentifier implements Comparator<HierarchicalIdentifier>, Comparable<HierarchicalIdentifier> {
	private final String identifier;
	private final String nodeType;
	public final String referenceIdentifier;

	public static final HierarchicalIdentifier UNDEFINED  = new HierarchicalIdentifier("UNDEFINED", "UNDEFINED");

	public String getIdentifier() {
		if (this == UNDEFINED) {
			throw new RuntimeException("The identifier for the specified node is undefined!");
		}
		return identifier;
	}

	public String getNodeType() {
		if (this == UNDEFINED) {
			throw new RuntimeException("The identifier for the specified node is undefined!");
		}
		return nodeType;
	}

	public HierarchicalIdentifier(@Nonnull String identifier, @Nonnull String nodeType) {
		this.identifier = identifier;
		this.nodeType = nodeType;
		this.referenceIdentifier = null;
	}

	public HierarchicalIdentifier(@Nonnull String referenceIdentifier) {
		this.identifier = null;
		this.nodeType = null;
		this.referenceIdentifier = referenceIdentifier;
	}

	public HierarchicalIdentifier(@Nonnull String identifier, @Nonnull String nodeType, @Nonnull String referenceIdentifier) {
		this.identifier = identifier;
		this.nodeType = nodeType;
		this.referenceIdentifier = referenceIdentifier;
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
				if (o.getIdentifier().equals(t1.getIdentifier())) {
					if (o.getNodeType().equals(t1.getNodeType())) {
						return 0;
					} else {
						throw new RuntimeException("Comparison of node with identifier '" + o.getIdentifier() + "' has indicated multiple node types of '" + o.getNodeType() + "' and '" + t1.getNodeType() + "'!");
					}

				} else {
					return o.getIdentifier().compareTo(t1.getIdentifier());
				}
			}
		}
	}

	@Override
	public int hashCode() {
		return (identifier + nodeType).hashCode();
	}

	public String toString() {
		String nodeTypeStr = getNodeType() == null ? "?" : getNodeType();
		String identifierStr = getIdentifier() == null ? "?" : getIdentifier();
		String rval = "v(" + nodeTypeStr + ")[" + identifierStr + "]";
		if (referenceIdentifier != null) {
			rval = rval + "{ID=" + referenceIdentifier + "}";
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
