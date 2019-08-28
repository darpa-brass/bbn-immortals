package mil.darpa.immortals.flitcons.validation;

import mil.darpa.immortals.flitcons.Configuration;
import mil.darpa.immortals.flitcons.datatypes.hierarchical.DuplicateInterface;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;

public class DebugData implements DuplicateInterface<DebugData> {
	private final String dataType;
	private final String dataReference;
	private static final boolean useSimpleLabels = Configuration.getInstance().validation.useSimpleLabels;
	private final LinkedHashMap<String, String> associatedData = new LinkedHashMap<>();

	public synchronized void addAttribute(@Nonnull String label, @Nonnull String value) {
		if (associatedData.containsKey(label)) {
			throw new RuntimeException("Map already contains debug value with the label '" + label + "'!");
		}
		associatedData.put(label, value);
	}

	public void removeAttribute(@Nonnull String attributeName) {
		associatedData.remove(attributeName);
	}

	public int getAttributeSize() {
		return associatedData.size();
	}

	public DebugData(@Nonnull String dataType, @Nullable String dataReference) {
		this.dataType = dataType;
		this.dataReference = dataReference;
	}

	@Override
	public DebugData duplicate() {
		DebugData dd = new DebugData(dataType, dataReference);
		dd.associatedData.putAll(this.associatedData);
		return dd;
	}


	public String toString() {
		if (useSimpleLabels) {
			if (associatedData.size() > 0) {
				return (String.join("/", associatedData.values()));
			} else {
				return dataType;
			}

		} else {
			StringBuilder sb = new StringBuilder();

			if (dataReference != null) {
				sb.append("v(").append(dataReference).append(")");
			}
			sb.append(dataType);

			StringBuilder attrBuilder = new StringBuilder();
			for (String key : associatedData.keySet()) {
				if (attrBuilder.length() == 0) {
					attrBuilder.append("{").append(key).append("=").append(associatedData.get(key));
				} else {
					attrBuilder.append(",").append(key).append("=").append(associatedData.get(key));
				}
			}
			if (attrBuilder.length() > 0) {
				attrBuilder.append("}");
				sb.append(attrBuilder.toString());
			}
			if (sb.length() == 0) {
				return null;
			} else {
				return sb.toString();
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DebugData)) {
			return false;
		}
		DebugData debugData = (DebugData) o;

		if (!dataType.equals(debugData.dataType)) {
			return false;
		}

		if (dataReference == null || debugData.dataReference == null) {
			if (dataReference != null || debugData.dataReference != null) {
				return false;
			}
		} else {
			if (!dataReference.equals(debugData.dataReference)) {
				return false;
			}
		}

		if (!associatedData.keySet().equals(debugData.associatedData.keySet())) {
			return false;
		}

		for (String key : associatedData.keySet()) {
			if (!associatedData.get(key).equals(debugData.associatedData.get(key))) {
				return false;
			}
		}
		return true;
	}
}
