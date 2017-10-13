package com.bbn.marti.dataservices;

public enum QueryResultStatusValue {

	EXECUTION_ERROR("Execution error"),
	CONSTRUCTION_ERROR("Construction error"),
	SUCCESSFUL("Successful");
	
	private final String value;

	QueryResultStatusValue(String value) {
		this.value = value;
    }

    public String value() {
        return value;
    }

	public static QueryResultStatusValue fromValue(String v) {
    	
		QueryResultStatusValue result = null;
    	
        for (QueryResultStatusValue c: QueryResultStatusValue.values()) {
            if (c.value.equals(v)) {
                result = c;
            }
        }
        
        return result;
    }

}
