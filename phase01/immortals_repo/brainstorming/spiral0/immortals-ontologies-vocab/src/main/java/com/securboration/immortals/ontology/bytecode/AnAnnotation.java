package com.securboration.immortals.ontology.bytecode;

/**
 * Model of an annotation
 * 
 * @author Securboration
 *
 */
public class AnAnnotation extends ClassStructure {

    private String annotationClassName;
    
    private AnnotationKeyValuePair[] keyValuePairs;

    public String getAnnotationClassName() {
        return annotationClassName;
    }

    public void setAnnotationClassName(String annotationClassName) {
        this.annotationClassName = annotationClassName;
    }

    public AnnotationKeyValuePair[] getKeyValuePairs() {
        return keyValuePairs;
    }

    public void setKeyValuePairs(AnnotationKeyValuePair[] keyValuePairs) {
        this.keyValuePairs = keyValuePairs;
    }
    
    

}
