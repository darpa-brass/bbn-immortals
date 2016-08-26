package com.securboration.immortals.ontology.deployment.uml;

/**
 * A relationship in a UML diagram
 * 
 * @author Securboration
 *
 */
public class DiagramRelationship {
   
    /**
     * The from node
     */
    private DiagramNode from;
    
    /**
     * The type of relationship being asserted
     */
    private String relationshipName;
    
    /**
     * The to node
     */
    private DiagramNode to;
    
    /**
     * The multiplicity on the from node
     */
    private Multiplicity fromMultiplicity;
    
    /**
     * The multiplicity on the to node
     */
    private Multiplicity toMultiplicity;
    
    public DiagramNode getFrom() {
        return from;
    }
    public void setFrom(DiagramNode from) {
        this.from = from;
    }
    public DiagramNode getTo() {
        return to;
    }
    public void setTo(DiagramNode to) {
        this.to = to;
    }
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
    public String getRelationshipName() {
        return relationshipName;
    }
    public void setRelationshipName(String relationshipName) {
        this.relationshipName = relationshipName;
    }
    
    
    
}
