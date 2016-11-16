package com.securboration.immortals.ontology.measurement;

import com.securboration.immortals.ontology.property.Property;

public class MeasurementInstance {
    
    private String qualifier;
    private Property measuredValue;
    
    public String getQualifier() {
        return qualifier;
    }

    
    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }


    
    public Property getMeasuredValue() {
        return measuredValue;
    }


    
    public void setMeasuredValue(Property measuredValue) {
        this.measuredValue = measuredValue;
    }
    
    
    

}
