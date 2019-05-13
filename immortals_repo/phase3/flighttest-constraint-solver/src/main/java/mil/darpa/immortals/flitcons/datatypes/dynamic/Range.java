package mil.darpa.immortals.flitcons.datatypes.dynamic;

import mil.darpa.immortals.flitcons.datatypes.hierarchical.DuplicateInterface;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import org.apache.commons.lang.math.NumberUtils;

/**
 * A simple range object
 */
public class Range implements DuplicateInterface<Range> {
	public final Object Min;
	public final Object Max;

	public Range(Object min, Object max) {
		if ((min instanceof Float && max instanceof Float) || (min instanceof Long && max instanceof Long)) {
			Min = min;
			Max = max;

		} else if (min instanceof String && max instanceof String && NumberUtils.isNumber((String) min) && NumberUtils.isNumber((String) max)) {
			String minStr = (String) min;
			String maxStr = (String) max;
			if (minStr.contains(".") || maxStr.contains(".")) {
				Min = Float.parseFloat(minStr);
				Max = Float.parseFloat(maxStr);
			} else {
				Min = Long.parseLong(minStr);
				Max = Long.parseLong(maxStr);
			}

		} else {
			throw AdaptationnException.input("Min and Max must both either be a float or a long!");
		}
	}

	public String toString() {
		return Min + " - " + Max;
	}

	public boolean fits(long value) {
		return (value <= (long) Max && value >= (long) Min);
	}

	public boolean fits(float value) {
		return (value <= (float) Max && value >= (float) Min);
	}

	@Override
	public Range duplicate() {
		return new Range(Min, Max);
	}
}
