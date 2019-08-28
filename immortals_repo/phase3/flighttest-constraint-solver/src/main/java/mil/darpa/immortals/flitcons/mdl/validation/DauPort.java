package mil.darpa.immortals.flitcons.mdl.validation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static mil.darpa.immortals.flitcons.Utils.Sym.EE;
import static mil.darpa.immortals.flitcons.Utils.Sym.NEE;

public class DauPort {

	public final String parentDauId;
	public final String id;
	public boolean isFlaggedForRemoval;
	public Requirements requirements = new Requirements();
	public String thermocouple;
	public String portType;
	public String direction;
	public boolean excitationPortIsPresent;

	public DauPort(@Nonnull String id, @Nonnull String parentDauId) {
		this.id = id;
		this.parentDauId = parentDauId;
	}

	public static class PortMeasurementCombination {
		public Long sampleRate;
		public Long dataLength;
		public Long dataRate;

		public boolean fits(Measurement chosenMeasurement) {
			return (sampleRate == null || sampleRate.equals(chosenMeasurement.sampleRate)) &&
					(dataLength == null || dataLength.equals(chosenMeasurement.dataLength)) &&
					(dataRate == null || dataRate.equals(chosenMeasurement.dataRate));
		}

		public String toString() {
			return Measurement.toString(sampleRate, dataLength, dataRate);
		}
	}

	public static class Requirements {
		public List<PortMeasurementCombination> validMeasurementCombinations = new LinkedList<>();
		public List<String> validThermocouples = new LinkedList<>();


		public void fits(@Nonnull Measurement measurement, @Nonnull DisplayablePortMapping displayablePortMapping) {
			try {
				displayablePortMapping.setMeasurementSelectionResult(fits(validMeasurementCombinations, measurement), true);
			} catch (ValidationFailureException e) {
				displayablePortMapping.setMeasurementSelectionResult(e.getMessage(), false);
			}
		}

		private String fits(@Nullable List<PortMeasurementCombination> validMeasurementCombinations,
		                    @Nullable Measurement actualMeasurement) throws ValidationFailureException {
			if (validMeasurementCombinations == null || validMeasurementCombinations.isEmpty()) {
				if (actualMeasurement != null) {
					throw new ValidationFailureException(actualMeasurement.toString() + " != null");
				}
				return "null==null";
			}

			String measurementOptions = validMeasurementCombinations.stream().map(PortMeasurementCombination::toString).collect(Collectors.joining());

			Optional<PortMeasurementCombination> selection = validMeasurementCombinations.stream().filter(x ->
					x.fits(actualMeasurement)
			).findFirst();

			if (selection.isPresent()) {
				return actualMeasurement.toString() + EE + "[" + measurementOptions + "]";

			} else {
				throw new ValidationFailureException(actualMeasurement.toString() + NEE + "[" + measurementOptions + "]");
			}
		}

		private String fits(@Nullable List<String> validValues, @Nullable String value) throws ValidationFailureException {
			if (validValues == null || validValues.isEmpty()) {
				if (value != null) {
					throw new ValidationFailureException("\"" + value + "\" != null");
				}
				return "null==null";
			}

			String validValuesDisplayString = "[\"" + String.join("\",\"", validValues) + "\"]";

			if (value == null) {
				throw new ValidationFailureException("null" + NEE + validValuesDisplayString);
			}
			if (validValues.contains(value)) {
				return "\"" + value + "\"" + EE + validValuesDisplayString;
			} else {
				throw new ValidationFailureException("\"" + value + "\"" + NEE + validValuesDisplayString);
			}
		}

		public void fits(@Nonnull DauPort dauPort, @Nonnull DisplayablePortMapping displayablePortMapping) {
			try {
				displayablePortMapping.setThermocoupleResult(fits(validThermocouples, dauPort.thermocouple), true);
			} catch (ValidationFailureException e) {
				displayablePortMapping.setThermocoupleResult(e.getMessage(), false);
			}
		}

	}
	// TODO: Do I need to consider DataRate?
}
