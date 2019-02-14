package mil.darpa.immortals.core.das;

public enum AssertionCriterionValue {

    LESS_THAN_INCLUSIVE("VALUE_LESS_THAN_INCLUSIVE");

	private final String value;

	AssertionCriterionValue(String value) {
		this.value = value;
    }

    public String value() {
        return value;
    }

	public static AssertionCriterionValue fromValue(String v) {
    	
		AssertionCriterionValue result = null;
    	
        for (AssertionCriterionValue c: AssertionCriterionValue.values()) {
            if (c.value.equals(v)) {
                result = c;
            }
        }
        
        return result;
    }
	
}
