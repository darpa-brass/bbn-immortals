package mil.darpa.immortals.dfus.TakServerDataManager;

import java.math.BigDecimal;

public class Utilities {
	
	private Utilities() {}
	
	public static String nullStringTo(String value, String convertTo) {
		
		if (value == null || value.trim().length() == 0) {
			return convertTo;
		} else {
			return value;
		}
	}
	
	public static float nullBigDecimalToFloat(BigDecimal value, float convertTo) {
		
		if (value == null) {
			return convertTo;
		} else {
			return value.floatValue();
		}
		
	}
}
