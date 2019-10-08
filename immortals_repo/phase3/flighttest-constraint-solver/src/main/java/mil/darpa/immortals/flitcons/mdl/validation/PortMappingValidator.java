package mil.darpa.immortals.flitcons.mdl.validation;

import mil.darpa.immortals.EnvironmentConfiguration;
import mil.darpa.immortals.flitcons.Utils;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static mil.darpa.immortals.flitcons.Utils.Sym.*;

public class PortMappingValidator {

	private static final Logger logger = LoggerFactory.getLogger(PortMappingValidator.class);

	private final Map<String, PortMapping> initialData;

	private static void _assert(boolean value, @Nonnull String message) {
		throw AdaptationnException.internal(message);
	}

	public PortMappingValidator(@Nonnull Map<String, PortMapping> initialData) {
		this.initialData = initialData;
	}

	public static void validateEqual(@Nonnull String label, @Nullable String originalValue, @Nullable String subsequentValue,
	                                 @Nonnull Map<String, Object> columnData, Map<String, Boolean> passData) {

		if (originalValue == null) {
			if (subsequentValue == null) {
				columnData.put(label, "null==null");
				passData.put(label, true);
			} else {
				columnData.put(label, "null!=" + subsequentValue);
				passData.put(label, false);
			}
		} else {
			if (subsequentValue == null) {
				columnData.put(label, originalValue + "!= null");
				passData.put(label, false);
			} else {
				if (originalValue.equals(subsequentValue)) {
					columnData.put(label, originalValue + "==" + subsequentValue);
					passData.put(label, true);
				} else {
					columnData.put(label, originalValue + "!=" + subsequentValue);
					passData.put(label, false);
				}
			}
		}
	}

	public static void validateEqualOrNullToAny(@Nonnull String label, @Nullable String originalValue, @Nullable String subsequentValue,
	                                            @Nonnull Map<String, Object> columnData, Map<String, Boolean> passData) {
		if (originalValue == null) {
			if (subsequentValue == null) {
				columnData.put(label, "null==null");
				passData.put(label, true);
			} else {
				columnData.put(label, "null" + EQT + subsequentValue);
				passData.put(label, true);
			}
		} else {
			if (subsequentValue == null) {
				columnData.put(label, originalValue + "!= null");
				passData.put(label, false);
			} else {
				if (originalValue.equals(subsequentValue)) {
					columnData.put(label, originalValue + "==" + subsequentValue);
					passData.put(label, true);
				} else {
					columnData.put(label, originalValue + "!=" + subsequentValue);
					passData.put(label, false);
				}
			}
		}
	}

	public static void validateResultDataPortMappings(@Nonnull Map<String, PortMapping> initialData, @Nonnull Map<String, PortMapping> resultData, @Nonnull String title) {
		Utils.ChartData cd = new Utils.ChartData(title, EnvironmentConfiguration.isBasicDisplayMode());

		// TODO: Add Measurement, DataStream, and Device data, accounting for multiplicities

		Set<String> badPortIdentifiers = initialData.values().stream().flatMap(pm -> pm.dauPorts.values().stream().filter(
				p -> p.isFlaggedForRemoval).map(p -> p.id)).collect(Collectors.toSet());

		TreeSet<String> combinedMappingIds = new TreeSet<>(initialData.keySet());
		combinedMappingIds.addAll(resultData.keySet());

		for (String mappingId : combinedMappingIds) {
			LinkedHashMap<String, Object> columnData = new LinkedHashMap<>();
			LinkedHashMap<String, Boolean> passData = new LinkedHashMap<>();
			cd.rowColumnData.put(mappingId, columnData);
			cd.validityMap.put(mappingId, passData);

			PortMapping initialPortMapping = initialData.get(mappingId);
			PortMapping resultPortMapping = resultData.get(mappingId);

			if (initialPortMapping == null) {
				columnData.put("Initially", NTE);
				passData.put("Initially", false);
				columnData.put("Subsequently", TE);
				passData.put("Subsequently", false);

			} else if (resultPortMapping == null) {
				columnData.put("Initially", TE);
				passData.put("Initially", false);
				columnData.put("Subsequently", NTE);
				passData.put("Subsequently", false);

			} else {
				columnData.put("Initially", TE);
				passData.put("Initially", true);
				columnData.put("Subsequently", TE);
				passData.put("Subsequently", true);
			}

			PortMapping initialMapping = initialData.get(mappingId);
			Map<String, DauPort> initialDauPorts = initialMapping.dauPorts;
			PortMapping resultMapping = resultData.get(mappingId);
			Map<String, DauPort> resultDauPorts = resultMapping.dauPorts;

			String initialPortsDisplayString = (initialDauPorts.size() == 0 ? "[]" :
					("[\"" + String.join("\",\"", initialDauPorts.keySet()) + "\"]"));

			String repairedPortsDisplayString = (resultDauPorts.size() == 0 ? "[]" :
					("[\"" + String.join("\",\"", resultDauPorts.keySet()) + "\"]"));

			if (initialDauPorts.size() == resultDauPorts.size()) {
				columnData.put("DAU Port Replacement", initialPortsDisplayString + EQT + repairedPortsDisplayString);
				passData.put("DAU Port Replacement", true);

			} else {
				columnData.put("DAU Port Replacement", initialPortsDisplayString + NEQT + repairedPortsDisplayString);
				passData.put("DAU Port Replacement", false);
			}

			if (resultMapping.dauPorts.size() > 1) {
				throw AdaptationnException.internal("Multiple DAU Ports shouldn't be in a PortMapping!");
			}

			if (initialMapping.dauPorts.size() > 1) {
				throw AdaptationnException.internal("Multiple DAU Ports shouldn't be in a PortMapping!");
			}

			for (String resultDauPortId : resultMapping.dauPorts.keySet()) {
				DauPort subsequent = resultMapping.dauPorts.get(resultDauPortId);
				DauPort original = initialData.get(mappingId).dauPorts.values().iterator().next();

				validateEqual("PortType", original.portType, subsequent.portType, columnData, passData);
				validateEqualOrNullToAny("Thermocouple", original.thermocouple, subsequent.thermocouple, columnData, passData);
				validateEqualOrNullToAny("PortPolarity", original.portPolarity, subsequent.portPolarity, columnData, passData);
				validateEqual("PortDirection", original.direction, subsequent.direction, columnData,passData);

				if (badPortIdentifiers.contains(resultDauPortId)) {
					columnData.put("Replacement Faulty", "true");
					passData.put("Replacement Faulty", false);
				} else {
					columnData.put("Replacement Faulty", "false");
					passData.put("Replacement Faulty", true);
				}
			}

			// TODO: Implement
//			for (String resultDevicePortId : resultMapping.devicePorts.keySet()) {
//			}

			// TODO: Implement
//			for (String resultMeasurementPort : resultMapping.measurements.keySet()) {
//			}
		}
		for (String line : Utils.makeChart(cd)) {
			logger.info(line);
		}
	}

	public void validateInitialData() {
		validateDataValues(initialData, "Initial Constraint Satisfaction");
	}

	public void validateResultData(@Nonnull Map<String, PortMapping> resultData) {
		validateDataValues(resultData, "Result Constraint Satisfaction");
		validateResultDataPortMappings(initialData, resultData, "PortMapping Result Delta");
	}

	public static void validateDataValues(@Nonnull Map<String, PortMapping> data, @Nonnull String title) {
		DisplayablePortMappings portMappingResults = new DisplayablePortMappings();
		Set<String> newKeys = data.keySet();

		for (String portMappingKey : newKeys) {
			DisplayablePortMapping portMappingResult = new DisplayablePortMapping(portMappingKey);
			portMappingResults.add(portMappingResult);

			PortMapping result = data.get(portMappingKey);

			if (result.dauPorts.isEmpty()) {
				throw AdaptationnException.input("All PortMappings must contain a DAU Port!");
			} else if (result.dauPorts.size() > 1) {
				throw AdaptationnException.input("No more than a single DAU Port is expected in a PortMapping!");
			}

			if (result.measurements.isEmpty() && result.dataStreams.isEmpty()) {
				throw AdaptationnException.input("All PortMappings must contain DataStream or Measurement!");
			}

			if (result.measurements.size() > 1) {
				throw AdaptationnException.input("Only a single measurement should be used within a PortMapping!");
			}

			// TODO: What about datastream port lengths?
			if (!result.devicePorts.isEmpty()) {
				if (result.measurements.isEmpty()) {
					throw AdaptationnException.input("All PortMappings that contain a Device Port must contain a Measurement!");
				}
				if (!result.dataStreams.isEmpty()) {
					throw AdaptationnException.input("All PortMappings that contain a Device Port cannot contain a DataStream!");
				}
			}

			if (!result.dataStreams.isEmpty()) {
				// TODO: Are all these N/A?
				portMappingResult.setMeasurementSelectionResult("N/A", true);
				portMappingResult.setDataLengthRangeResult("N/A", true);
				portMappingResult.setDataRateRangeResult("N/A", true);
				portMappingResult.setDirectionResult("N/A", true);
				portMappingResult.setExcitationResult("N/A", true);
				portMappingResult.setPortTypeResult("N/A", true);
				portMappingResult.setSampleRateRangeResult("N/A", true);
				portMappingResult.setThermocoupleResult("N/A", true);
				// The Datastream simply being present indicates this is successful other than validating general details
				portMappingResult.setDataStreamResult("PRESENT", true);

				// TODO: Add validation of general details

			} else if (!result.devicePorts.isEmpty()) {
				if (result.dauPorts.size() > 1 || result.devicePorts.size() > 1) {
					// TODO: get Example of this
					throw AdaptationnException.internal("Not yet supported!");

				} else {
					for (DauPort dauPort : result.dauPorts.values()) {
						dauPort.requirements.fits(dauPort, portMappingResult);

						for (Measurement measurement : result.measurements.values()) {
							dauPort.requirements.fits(measurement, portMappingResult);
							measurement.requirements.fits(measurement, portMappingResult);
						}
						for (DevicePort devicePort : result.devicePorts.values()) {
							devicePort.fits(dauPort, portMappingResult);
						}
						portMappingResult.setDataStreamResult("N/A", true);
					}
				}

			} else {
				// TODO: Get example of this
				throw new RuntimeException("No DAU ports were detected in the PortMapping!");
			}
		}

		List<String> results = portMappingResults.makeChart(title);
		for (String line : results) {
			logger.info(line);
		}
	}
}
