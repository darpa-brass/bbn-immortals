package mil.darpa.immortals.flitcons.mdl.validation;

import mil.darpa.immortals.flitcons.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class DisplayablePortMappings extends LinkedList<DisplayablePortMapping> {

	private void addValue(@Nonnull LinkedHashMap<String, Object> columnData, @Nonnull LinkedHashMap<String, Boolean> validityMap, @Nonnull String header, @Nullable Object value, Boolean isValid) {
		boolean hasValue = value != null;

		if (hasValue) {
			columnData.put(header, value);
		} else {
			columnData.put(header, "");
		}
		validityMap.put(header, isValid);
	}

	public List<String> makeChart(@Nonnull String title) {
		TreeMap<String, Map<String, Object>> rowColumnData = new TreeMap<>();
		TreeMap<String, Map<String, Boolean>> validityMap = new TreeMap<>();

		for (DisplayablePortMapping portMapping : this) {
			LinkedHashMap<String, Object> columnMap = new LinkedHashMap<>();
			rowColumnData.put(portMapping.getId(), columnMap);
			LinkedHashMap<String, Boolean> columnColorMap = new LinkedHashMap<>();
			validityMap.put(portMapping.getId(), columnColorMap);
			addValue(columnMap, columnColorMap, "DataRateRange", portMapping.getDataRateRangeDisplayString(), portMapping.isDataRateRangePass());
			addValue(columnMap, columnColorMap, "DataRateSelection", portMapping.getDataRateSelectionDisplayString(), portMapping.isDataRateSelectionPass());
			addValue(columnMap, columnColorMap, "DataLengthRange", portMapping.getDataLengthRangeDisplayString(), portMapping.isDataLengthRangePass());
			addValue(columnMap, columnColorMap, "DataLengthSelection", portMapping.getDataLengthSelectionDisplayString(), portMapping.isDataLengthSelectionPass());
			addValue(columnMap, columnColorMap, "SampleRateRange", portMapping.getSampleRateRangeDisplayString(), portMapping.isSampleRateRangePass());
			addValue(columnMap, columnColorMap, "SampleRateSelection", portMapping.getSampleRateSelectionDisplayString(), portMapping.isSampleRateSelectionPass());
			addValue(columnMap, columnColorMap, "Direction", portMapping.getDirectionDisplayString(), portMapping.isDirectionPass());
			addValue(columnMap, columnColorMap, "Excitation", portMapping.getExcitationDisplayString(), portMapping.isExcitationPass());
			addValue(columnMap, columnColorMap, "Thermocouple", portMapping.getThermocoupleDisplayString(), portMapping.isThermocouplePass());
			// TODO: Also check PortType?
		}
		return Utils.makeChart(rowColumnData, validityMap, null, title);
	}

	public boolean isValid() {
		for (DisplayablePortMapping pm : this) {
			if (!(pm.isDataRateRangePass() &&
					pm.isDataRateSelectionPass() &&
					pm.isDataLengthRangePass() &&
					pm.isDataLengthSelectionPass() &&

					pm.isSampleRateRangePass() &&
					pm.isSampleRateSelectionPass() &&
					pm.isDirectionPass() &&
					pm.isExcitationPass() &&
					pm.isPortTypePass() &&
					pm.isThermocouplePass()
			)) {
				return false;
			}
		}
		return true;
	}
}
