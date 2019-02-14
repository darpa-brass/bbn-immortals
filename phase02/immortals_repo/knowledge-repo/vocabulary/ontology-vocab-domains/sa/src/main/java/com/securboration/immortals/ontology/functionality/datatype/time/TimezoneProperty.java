package com.securboration.immortals.ontology.functionality.datatype.time;

public class TimezoneProperty extends TemporalProperty {
    
    private int gmtOffset;
    
    private String tag;

    public int getGmtOffset() {
        return gmtOffset;
    }

    public void setGmtOffset(int gmtOffset) {
        this.gmtOffset = gmtOffset;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
    
}
