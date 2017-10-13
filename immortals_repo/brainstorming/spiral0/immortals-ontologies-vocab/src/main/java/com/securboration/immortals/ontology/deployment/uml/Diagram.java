package com.securboration.immortals.ontology.deployment.uml;

/**
 * Model of a UML diagram
 * 
 * @author Securboration
 *
 */
public class Diagram {
    
    /**
     * The name of the diagram
     */
    private String name;
    
    /**
     * The nodes in the diagram
     */
    private DiagramNodeSet[] nodes;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public DiagramNodeSet[] getNodes() {
        return nodes;
    }
    public void setNodes(DiagramNodeSet[] nodes) {
        this.nodes = nodes;
    }

}
