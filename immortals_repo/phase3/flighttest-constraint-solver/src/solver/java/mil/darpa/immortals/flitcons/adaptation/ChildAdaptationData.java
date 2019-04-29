package mil.darpa.immortals.flitcons.adaptation;

import javax.annotation.Nonnull;
import java.util.TreeMap;

public class ChildAdaptationData {

	public final String parentGloballyUniqueId;
	public final String globallyUniqueId;
	public final String supersededPortUniqueId;
	public final TreeMap<String, Object> values;

	public ChildAdaptationData(@Nonnull String parentGloballyUniqueId, @Nonnull String globallyUniqueId, @Nonnull String supersededPortUniqueId) {
		this.parentGloballyUniqueId = parentGloballyUniqueId;
		this.globallyUniqueId = globallyUniqueId;
		this.supersededPortUniqueId = supersededPortUniqueId;
		values = new TreeMap<>();
	}
}
