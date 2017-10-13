package com.securboration.immortals.ontology.deployment.uml3;

/**
 * A KV pair for use in a diagram
 * 
 * 
 * @author Securboration
 *
 */
public class KeyValuePair {
    
    /**
     * A key associated with some value
     */
    private String key;
    
    /**
     * The value associated with the key
     */
    private Object value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

}
