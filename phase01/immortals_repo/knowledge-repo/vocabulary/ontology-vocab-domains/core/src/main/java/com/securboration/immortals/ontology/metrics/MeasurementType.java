package com.securboration.immortals.ontology.metrics;

import com.securboration.immortals.ontology.property.Property;

public class MeasurementType {
    
    private String measurementType;
    
    private Class<? extends Property> correspondingProperty;

    
    public String getMeasurementType() {
        return measurementType;
    }

    
    public void setMeasurementType(String measurementType) {
        this.measurementType = measurementType;
    }


    
    public Class<? extends Property> getCorrespondingProperty() {
        return correspondingProperty;
    }


    
    public void setCorrespondingProperty(
            Class<? extends Property> indicativeOfPropertyType) {
        this.correspondingProperty = indicativeOfPropertyType;
    }

}
