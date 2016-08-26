package mil.darpa.immortals.core.das;

public enum AdaptationStatusValue {

    SUCCESSFUL("SUCCESSFUL", "IMMORTALS successfully adapted system for deployment model."),
    UNSUCCESSFUL("UNSUCCESFUL", "IMMORTALS could not successfully adapt system for deployment model."),
    PENDING("PENDING", "Adaptation status pending."),
    ERROR("ERROR", "IMMORTALS encountered an an unexpected error.");

	private final String value;
	private final String description;

	AdaptationStatusValue(String value, String description) {
		this.value = value;
		this.description = description;
    }

    public String value() {
        return value;
    }
    
    public String description() {
    	return description;
    }

	public static AdaptationStatusValue fromValue(String v) {
    	
		AdaptationStatusValue result = null;
    	
        for (AdaptationStatusValue c: AdaptationStatusValue.values()) {
            if (c.value.equals(v)) {
                result = c;
            }
        }
        
        return result;
    }
	
}
