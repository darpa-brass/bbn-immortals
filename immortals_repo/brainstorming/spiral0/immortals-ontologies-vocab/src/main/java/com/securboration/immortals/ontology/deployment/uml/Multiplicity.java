package com.securboration.immortals.ontology.deployment.uml;

/**
 * A multiplicity on a relationship edge endpoint
 * 
 * 
 * @author Securboration
 *
 */
public class Multiplicity {

    /**
     * The lower bound of a multiplicity. May be null, in which case no lower
     * bound exists
     */
    private MultiplicityBound lowerBound;

    /**
     * The upper bound of a multiplicity. May be null, in which case no upper
     * bound exists
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
