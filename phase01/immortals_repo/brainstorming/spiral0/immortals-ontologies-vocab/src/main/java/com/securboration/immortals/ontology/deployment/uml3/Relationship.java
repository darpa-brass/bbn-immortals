package com.securboration.immortals.ontology.deployment.uml3;

/**
 * A relationship between two diagram nodes
 * 
 * @author Securboration
 *
 */
public class Relationship {
   
    /**
     * The "from" node in the relationship
     */
    private DiagramNode fromNode;
    
    /**
     * The "to" node in the relationship
     */
    private DiagramNode toNode;

    public DiagramNode getFromNode() {
        return fromNode;
    }

    public void setFromNode(DiagramNode fromNode) {
        this.fromNode = fromNode;
    }

    public DiagramNode getToNode() {
        return toNode;
    }

    public void setToNode(DiagramNode toNode) {
        this.toNode = toNode;
    }
}
