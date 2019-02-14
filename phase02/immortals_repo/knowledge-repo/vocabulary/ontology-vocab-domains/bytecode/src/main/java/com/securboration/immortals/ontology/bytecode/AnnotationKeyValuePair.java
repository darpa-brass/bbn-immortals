package com.securboration.immortals.ontology.bytecode;

/**
 * A key-value pair defined in an annotation
 * 
 * @author Securboration
 *
 */
public class AnnotationKeyValuePair extends ClassStructure {

    private String key;
    
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    

}
