package com.securboration.immortals.ontology.deployment.uml3;

/**
 * Models a UML diagram
 * 
 * @author Securboration
 *
 */
public class Diagram {
    
    /**
     * Nodes in the diagram
     */
    private DiagramNode[] nodes;
    
    /**
     * Relationships between nodes in the diagram
     */
    private Relationship[] relationships;

    public DiagramNode[] getNodes() {
        return nodes;
    }

    public void setNodes(DiagramNode[] nodes) {
        this.nodes = nodes;
    }

    public Relationship[] getRelationships() {
        return relationships;
    }

    public void setRelationships(Relationship[] relationships) {
        this.relationships = relationships;
    }

}
