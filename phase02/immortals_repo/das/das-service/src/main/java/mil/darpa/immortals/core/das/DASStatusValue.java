package mil.darpa.immortals.core.das;

public enum DASStatusValue {

    STOPPED("Stopped"),
    STOPPING("Stopping"),
    STARTING("Starting"),
	RUNNING("Running"),
    ERROR("Error"),
    OFFLINE_ADAPTATION("Offline Adaptation");

	private final String value;

	DASStatusValue(String value) {
		this.value = value;
    }

    public String value() {
        return value;
    }

	public static DASStatusValue fromValue(String v) {
    	
		DASStatusValue result = null;
    	
        for (DASStatusValue c: DASStatusValue.values()) {
            if (c.value.equals(v)) {
                result = c;
            }
        }
        
        return result;
    }
	
}
