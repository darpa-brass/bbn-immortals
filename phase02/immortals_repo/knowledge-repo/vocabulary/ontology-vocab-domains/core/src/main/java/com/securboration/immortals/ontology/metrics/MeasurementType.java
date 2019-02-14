package com.securboration.immortals.ontology.metrics;

import com.securboration.immortals.ontology.property.Property;

/**
 * A type of measurement
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A type of measurement  @author jstaples ")
public class MeasurementType {
    
    /**
     * A tag describing the measurement type
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A tag describing the measurement type")
    private String measurementType;
    
    /**
     * The property being measured
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The property being measured")
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
