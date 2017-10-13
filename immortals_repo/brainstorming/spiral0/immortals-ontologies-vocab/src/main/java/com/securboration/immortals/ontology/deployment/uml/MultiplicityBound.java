package com.securboration.immortals.ontology.deployment.uml;

/**
 * A bound on a multiplicity 
 * 
 * @author Securboration
 *
 */
public class MultiplicityBound {

    /**
     * The value of the bound
     */
    private int boundValue;
    
    /**
     * True iff the bound includes the bound value
     */
    private boolean isInclusive;
    
    public int getBoundValue() {
        return boundValue;
    }
    public void setBoundValue(int boundValue) {
        this.boundValue = boundValue;
    }
    public boolean isInclusive() {
        return isInclusive;
    }
    public void setInclusive(boolean isInclusive) {
        this.isInclusive = isInclusive;
    }
    
}
