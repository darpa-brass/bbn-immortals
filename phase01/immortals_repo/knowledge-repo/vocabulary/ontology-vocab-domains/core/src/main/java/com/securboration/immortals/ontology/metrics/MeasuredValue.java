package com.securboration.immortals.ontology.metrics;


public class MeasuredValue {
    
    private MeasurementType type;
    private String value;
    private String unit;
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }

    
    public MeasurementType getType() {
        return type;
    }

    
    public void setType(MeasurementType type) {
        this.type = type;
    }

}
