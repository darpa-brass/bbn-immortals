package com.securboration.immortals.ontology.deployment.uml;

/**
 * A node in a UML diagram
 * @author Securboration
 *
 */
public class DiagramNode {
    
    /**
     * The name of the node
     */
    private String name;
    
    /**
     * A unique id for the node
     */
    private String id;
    
    /**
     * The relationships of this node
     */
    private DiagramRelationship[] relationships;
    
    /**
     * The meta relationships of this node
     */
    private DiagramRelationship[] metaRelationships;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public DiagramRelationship[] getRelationships() {
        return relationships;
    }
    public void setRelationships(DiagramRelationship[] relationships) {
        this.relationships = relationships;
    }
    public DiagramRelationship[] getMetaRelationships() {
        return metaRelationships;
    }
    public void setMetaRelationships(DiagramRelationship[] metaRelationships) {
        this.metaRelationships = metaRelationships;
    }

}
