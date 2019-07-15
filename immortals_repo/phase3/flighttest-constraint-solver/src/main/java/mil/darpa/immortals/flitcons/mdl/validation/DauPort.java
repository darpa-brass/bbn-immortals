package mil.darpa.immortals.flitcons.mdl.validation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
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

	public static class Requirements {
		public List<Long> validSampleRates = new LinkedList<>();
		// TODO: Could this have multiple possible values?
		public List<Long> validDataLengths = new LinkedList<>();
		public List<Long> validDataRates = new LinkedList<>();
		public List<String> validThermocouples = new LinkedList<>();

		private String fits(@Nullable List<Long> validValues, @Nullable Long value, boolean allowValueWithoutRequirement) throws ValidationFailureException {
			if (validValues == null || validValues.isEmpty()) {
				if (allowValueWithoutRequirement) {
					if (value == null) {
						return "null";
					} else {
						return Long.toString(value);
					}

				} else {
					if (value != null) {
						throw new ValidationFailureException(value + " != null");
					}
					return "null==null";
				}
			}

			String validValuesDisplayString = "[" + validValues.stream().map(t -> Long.toString(t)).collect(Collectors.joining(",")) + "]";

			if (value == null) {
				throw new ValidationFailureException("null" + NEE + validValuesDisplayString);
			}
			if (validValues.contains(value)) {
				return value + EE + validValuesDisplayString;
			} else {
				throw new ValidationFailureException(value + NEE + validValuesDisplayString);
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

		public void fits(@Nonnull Measurement measurement, @Nonnull DisplayablePortMapping displayablePortMapping) {
			try {
				displayablePortMapping.setDataLengthSelectionResult(fits(validDataLengths, measurement.dataLength, false), true);
			} catch (ValidationFailureException e) {
				displayablePortMapping.setDataLengthSelectionResult(e.getMessage(), false);
			}

			try {
				displayablePortMapping.setDataRateSelectionResult(fits(validDataRates, measurement.dataRate, true), true);
			} catch (ValidationFailureException e) {
				displayablePortMapping.setDataRateSelectionResult(e.getMessage(), false);
			}

			try {
				displayablePortMapping.setSampleRateSelectionResult(fits(validSampleRates, measurement.sampleRate, false), true);
			} catch (ValidationFailureException e) {
				displayablePortMapping.setSampleRateSelectionResult(e.getMessage(), false);
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
