package mil.darpa.immortals.flitcons.mdl.validation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static mil.darpa.immortals.flitcons.Utils.Sym.LTE;
import static mil.darpa.immortals.flitcons.Utils.Sym.NLTE;


public class Measurement {

	public final String id;
	public Long dataLength;
	public Long dataRate;
	public Long sampleRate;
	public Requirements requirements = new Requirements();
	public Measurement(@Nonnull String id) {
		this.id = id;
	}

	public static class Requirements {
		public Long minDataLength;
		public Long maxDataLength;
		public Long minDataRate;
		public Long maxDataRate;
		public Long minSampleRate;
		public Long maxSampleRate;

		private String rangeCheck(@Nullable Long min, @Nullable Long max) throws ValidationFailureException {
			if (min == null && max == null) {
				return "N/A";

			} else if (min == null) {
				throw new ValidationFailureException("min == null");

			} else if (max == null) {
				throw new ValidationFailureException("max == null");

			} else {
				return min + " to " + max;
			}
		}

		public void validate(@Nonnull DisplayablePortMapping displayablePortMapping) {
			try {
				displayablePortMapping.setSampleRateRangeResult(rangeCheck(minSampleRate, maxSampleRate), true);
			} catch (ValidationFailureException e) {
				displayablePortMapping.setSampleRateRangeResult(e.getMessage(), false);
			}

			try {
				displayablePortMapping.setDataLengthRangeResult(rangeCheck(minDataLength, maxDataLength), true);
			} catch (ValidationFailureException e) {
				displayablePortMapping.setDataLengthRangeResult(e.getMessage(), false);
			}

			try {
				displayablePortMapping.setDataRateRangeResult(rangeCheck(minDataRate, maxDataRate), true);
			} catch (ValidationFailureException e) {
				displayablePortMapping.setDataRateRangeResult(e.getMessage(), false);
			}
		}

		private String fits(@Nullable Long min, @Nullable Long max, @Nullable Long value) throws ValidationFailureException {
			if (min == null && max == null) {
				if (value == null) {
					return "N/A";
				} else {
					throw new ValidationFailureException("null" + NLTE + value + NLTE + "null");
				}

			} else if (min == null || max == null) {
				throw new RuntimeException("Min or Max value is null!");

			} else {
				if (value == null) {
					throw new ValidationFailureException(min + NLTE + "null" + NLTE + max);

				} else {
					if (value >= min && value <= max) {
						return min + LTE + value + LTE + max;
					} else {
						String err = min + (min <= value ? LTE : NLTE) + value + (value <= max ? LTE : NLTE);
						throw new ValidationFailureException(err);
					}
				}
			}
		}

		public void fits(@Nonnull Measurement measurement, @Nonnull DisplayablePortMapping displayablePortMapping) {
			try {
				displayablePortMapping.setSampleRateRangeResult(fits(minSampleRate, maxSampleRate, measurement.sampleRate), true);
			} catch (ValidationFailureException e) {
				displayablePortMapping.setSampleRateRangeResult(e.getMessage(), false);
			}

			try {
				displayablePortMapping.setDataLengthRangeResult(fits(minDataLength, maxDataLength, measurement.dataLength), true);
			} catch (ValidationFailureException e) {
				displayablePortMapping.setDataLengthRangeResult(e.getMessage(), false);
			}

			try {
				displayablePortMapping.setDataRateRangeResult(fits(minDataRate, maxDataRate, measurement.dataRate), true);
			} catch (ValidationFailureException e) {
				displayablePortMapping.setDataRateRangeResult(e.getMessage(), false);
			}
		}
	}
}
