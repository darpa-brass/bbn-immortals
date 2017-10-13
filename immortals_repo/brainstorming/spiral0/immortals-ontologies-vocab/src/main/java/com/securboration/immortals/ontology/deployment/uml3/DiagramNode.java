package com.securboration.immortals.ontology.deployment.uml3;

/**
 * A KV pair for use in a diagram
 * 
 * 
 * @author Securboration
 *
 */
public class DiagramNode {
    
    /**
     * The name of the node
     */
    private String nodeName;
    
    /**
     * The attributes of the node
     */
    private KeyValuePair[] attributes;
    
    /**
     * The constraints on the node
     */
    private KeyValuePair[] constraints;
    
    /**
     * The aspects of the node
     */
    private KeyValuePair[] aspects;
    
    public KeyValuePair[] getAttributes() {
        return attributes;
    }
    public void setAttributes(KeyValuePair[] attributes) {
        this.attributes = attributes;
    }
    public KeyValuePair[] getConstraints() {
        return constraints;
    }
    public void setConstraints(KeyValuePair[] constraints) {
        this.constraints = constraints;
    }
    public KeyValuePair[] getAspects() {
        return aspects;
    }
    public void setAspects(KeyValuePair[] aspects) {
        this.aspects = aspects;
    }
    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

}
