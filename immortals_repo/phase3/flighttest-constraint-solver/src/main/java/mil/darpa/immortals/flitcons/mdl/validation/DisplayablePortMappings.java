package mil.darpa.immortals.flitcons.mdl.validation;

import mil.darpa.immortals.EnvironmentConfiguration;
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
		Utils.ChartData cd = new Utils.ChartData(title, EnvironmentConfiguration.isBasicDisplayMode());

		for (DisplayablePortMapping portMapping : this) {
			LinkedHashMap<String, Object> columnMap = new LinkedHashMap<>();
			cd.rowColumnData.put(portMapping.getId(), columnMap);
			LinkedHashMap<String, Boolean> columnColorMap = new LinkedHashMap<>();
			cd.validityMap.put(portMapping.getId(), columnColorMap);
			addValue(columnMap, columnColorMap, "DataRateRange", portMapping.getDataRateRangeDisplayString(), portMapping.isDataRateRangePass());
			addValue(columnMap, columnColorMap, "DataLengthRange", portMapping.getDataLengthRangeDisplayString(), portMapping.isDataLengthRangePass());
			addValue(columnMap, columnColorMap, "SampleRateRange", portMapping.getSampleRateRangeDisplayString(), portMapping.isSampleRateRangePass());
			addValue(columnMap, columnColorMap, "Measurement", portMapping.getMeasurementSelectionDisplayString(), portMapping.isMeasurementSelectionPass());
			addValue(columnMap, columnColorMap, "Direction", portMapping.getDirectionDisplayString(), portMapping.isDirectionPass());
			addValue(columnMap, columnColorMap, "Excitation", portMapping.getExcitationDisplayString(), portMapping.isExcitationPass());
			addValue(columnMap, columnColorMap, "Thermocouple", portMapping.getThermocoupleDisplayString(), portMapping.isThermocouplePass());
			addValue(columnMap, columnColorMap, "DataStream", portMapping.getDataStreamDisplayString(), portMapping.isDataStreamPass());
			// TODO: Also check PortType?
		}
		return Utils.makeChart(cd);
	}

	public boolean isValid() {
		for (DisplayablePortMapping pm : this) {
			if (!(pm.isDataRateRangePass() &&
					pm.isDataLengthRangePass() &&
					pm.isSampleRateRangePass() &&
					pm.isMeasurementSelectionPass() &&
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
