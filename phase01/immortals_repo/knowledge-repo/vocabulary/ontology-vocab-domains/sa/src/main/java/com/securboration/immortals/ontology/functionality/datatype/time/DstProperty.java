package com.securboration.immortals.ontology.functionality.datatype.time;

public class DstProperty extends TemporalProperty {
    
    private boolean usesDaylightSavingsTime;

    public boolean isUsesDaylightSavingsTime() {
        return usesDaylightSavingsTime;
    }

    public void setUsesDaylightSavingsTime(boolean usesDaylightSavingsTime) {
        this.usesDaylightSavingsTime = usesDaylightSavingsTime;
    }
    
}
