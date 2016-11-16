package com.securboration.immortals.ontology.measurement;

import com.securboration.immortals.ontology.property.Property;

/**
 * A profile linking a code unit to a set of measurements made about that code 
 * unit
 * 
 * @author jstaples
 *
 */
public class MetricProfile {
    
    /**
     * A human readable description
     */
    private String humanReadableDesc;
    
    /**
     * The code unit this measurement binds to
     */
    private CodeUnitPointer codeUnit;
    
    /**
     * Measurements made about this code unit
     */
    private Property[] measuredProperty;
    


    
    public CodeUnitPointer getCodeUnit() {
        return codeUnit;
    }


    
    public void setCodeUnit(CodeUnitPointer codeUnit) {
        this.codeUnit = codeUnit;
    }



    
    public Property[] getMeasuredProperty() {
        return measuredProperty;
    }



    
    public void setMeasuredProperty(Property[] measuredProperty) {
        this.measuredProperty = measuredProperty;
    }



    
    public String getHumanReadableDesc() {
        return humanReadableDesc;
    }



    
    public void setHumanReadableDesc(String humanReadableDesc) {
        this.humanReadableDesc = humanReadableDesc;
    }
    
}
