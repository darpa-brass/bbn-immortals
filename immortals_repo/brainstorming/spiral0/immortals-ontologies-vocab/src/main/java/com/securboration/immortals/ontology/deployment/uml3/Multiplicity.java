package com.securboration.immortals.ontology.deployment.uml3;

/**
 * A multiplicity on a relationship edge endpoint
 * 
 * 
 * @author Securboration
 *
 */
public class Multiplicity {
    
    /**
     * The lower bound, if one exists
     */
    private MultiplicityBound lowerBound;
    
    /**
     * The upper bound, if one exists
     */
    private MultiplicityBound upperBound;
    
    
    public MultiplicityBound getLowerBound() {
        return lowerBound;
    }
    public void setLowerBound(MultiplicityBound lowerBound) {
        this.lowerBound = lowerBound;
    }
    public MultiplicityBound getUpperBound() {
        return upperBound;
    }
    public void setUpperBound(MultiplicityBound upperBound) {
        this.upperBound = upperBound;
    }

}
