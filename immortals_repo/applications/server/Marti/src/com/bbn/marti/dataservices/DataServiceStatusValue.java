package com.bbn.marti.dataservices;

public enum DataServiceStatusValue {

	STARTING("Starting"),
    STARTED("Started"),
	STOPPING("Stopping"),
	STOPPED("Stopped");
	
	private final String value;

	DataServiceStatusValue(String value) {
		this.value = value;
    }

    public String value() {
        return value;
    }

	public static DataServiceStatusValue fromValue(String v) {
    	
		DataServiceStatusValue result = null;
    	
        for (DataServiceStatusValue c: DataServiceStatusValue.values()) {
            if (c.value.equals(v)) {
                result = c;
            }
        }
        
        return result;
    }

}
