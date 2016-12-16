package mil.darpa.immortals.core.das;

public enum DSLInvocationResultValue {

    MODEL_OK(0, "The model loads successfully and satisfies the mission requirements."),
    ERROR(1, "Miscellaneous Error -- see output for details (e.g. bad arguments, JSON decoding error, bug in interpreter)."),
    MODEL_DOES_NOT_LOAD(2, "The model cannot be loaded in the given environment."),
    MODEL_DOES_NOT_SATISFY(3, "The model successfully loads but does not satisfy the requirements.");

	private final int value;
	private final String description;

	DSLInvocationResultValue(int value, String description) {
		this.value = value;
		this.description = description;
    }

    public int value() {
        return value;
    }
    
    public String description() {
    	return description;
    }

	public static DSLInvocationResultValue fromValue(int v) {
    	
		DSLInvocationResultValue result = null;
    	
        for (DSLInvocationResultValue c: DSLInvocationResultValue.values()) {
            if (c.value == v) {
                result = c;
            }
        }
        
        return result;
    }
	
}
