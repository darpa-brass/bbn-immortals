package mil.darpa.immortals.flitcons.adaptation;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class ParentAdaptationData {
	public final String globallyUniqueId;
	public final Set<String> supersededDausUniqueIds;
	public final TreeMap<String, Object> values;
	public final Set<ChildAdaptationData> ports;

	public ParentAdaptationData(@Nonnull String globallyUniqueId, @Nonnull Set<String> supersededDausUniqueIds) {
		this.globallyUniqueId = globallyUniqueId;
		this.supersededDausUniqueIds = supersededDausUniqueIds;
		this.values = new TreeMap<>();
		this.ports = new HashSet<>();
	}
}
