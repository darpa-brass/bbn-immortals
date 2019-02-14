package com.securboration.immortals.ontology.unit;

/**
 * Describes a unit of measure 
 * 
 * @author jstaples
 *
 */
public class UnitOfMeasure {
    
    /**
     * A tag that uniquely identifies the UoM.  E.g., km/s, kg*m/s^2
     */
    private String unitOfMeasureTag;

    
    public String getUnitOfMeasureTag() {
        return unitOfMeasureTag;
    }

    
    public void setUnitOfMeasureTag(String unitOfMeasureTag) {
        this.unitOfMeasureTag = unitOfMeasureTag;
    }

}
