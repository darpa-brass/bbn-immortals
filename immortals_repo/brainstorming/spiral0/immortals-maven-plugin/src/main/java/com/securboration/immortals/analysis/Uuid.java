package com.securboration.immortals.analysis;

public class Uuid extends AnalysisBaseType {
    
    public Uuid(){super();}
    public Uuid(String s){super(s);}
    
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
