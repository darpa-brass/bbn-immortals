package mil.darpa.immortals.flitcons.datatypes;


import javax.annotation.Nonnull;
import java.util.Comparator;

public class HierarchicalNode implements Comparator<HierarchicalNode>, Comparable<HierarchicalNode>, DuplicateInterface<HierarchicalNode> {
	public final String identifier;
	public final String nodeType;

	public HierarchicalNode(@Nonnull String identifier, @Nonnull String nodeType) {
		this.identifier = identifier;
		this.nodeType = nodeType;
	}

	@Override
	public int compareTo(HierarchicalNode hierarchicalNode) {
		return compare(this, hierarchicalNode);
	}

	@Override
	public int compare(HierarchicalNode o, HierarchicalNode t1) {
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
				if (o.identifier.equals(t1.identifier)) {
					if (o.nodeType.equals(t1.nodeType)) {
						return 0;
					} else {
						throw new RuntimeException("Comparison of node with identifier '" + o.identifier + "' has indicated multiple node types of '" + o.nodeType + "' and '" + t1.nodeType + "'!");
					}

				} else {
					return o.identifier.compareTo(t1.identifier);
				}
			}
		}
	}

	@Override
	public int hashCode() {
		return (identifier + nodeType).hashCode();
	}

	public String toString() {
		return "v(" + nodeType + ")[" + identifier + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof HierarchicalNode)) {
			return false;
		} else {
			return compare(this, (HierarchicalNode) o) == 0;
		}
	}

	@Override
	public HierarchicalNode duplicate() {
		return new HierarchicalNode(identifier, nodeType);
	}
}
