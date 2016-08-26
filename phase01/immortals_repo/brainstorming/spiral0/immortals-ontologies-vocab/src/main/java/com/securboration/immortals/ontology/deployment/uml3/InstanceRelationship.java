package com.securboration.immortals.ontology.deployment.uml3;

/**
 * A relationship between two instances of a class
 * 
 * 
 * @author Securboration
 *
 */
public class InstanceRelationship extends Relationship {
    
    /**
     * The multiplicity of the "from" node
     */
    private Multiplicity fromMultiplicity;
    
    /**
     * The multiplicity of the "to" node
     */
    private Multiplicity toMultiplicity;
    
    /**
     * The direction of the edge
     */
    private EdgeDirection edgeDirection;

    public Multiplicity getFromMultiplicity() {
        return fromMultiplicity;
    }

    public void setFromMultiplicity(Multiplicity fromMultiplicity) {
        this.fromMultiplicity = fromMultiplicity;
    }

    public Multiplicity getToMultiplicity() {
        return toMultiplicity;
    }

    public void setToMultiplicity(Multiplicity toMultiplicity) {
        this.toMultiplicity = toMultiplicity;
    }

    public EdgeDirection getEdgeDirection() {
        return edgeDirection;
    }

    public void setEdgeDirection(EdgeDirection edgeDirection) {
        this.edgeDirection = edgeDirection;
    }
    
}
