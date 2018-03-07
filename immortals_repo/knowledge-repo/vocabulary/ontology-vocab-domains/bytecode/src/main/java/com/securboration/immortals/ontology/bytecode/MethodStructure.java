package com.securboration.immortals.ontology.bytecode;

/**
 * An abstraction specific to a method
 * 
 * @author Securboration
 *
 */
public class MethodStructure {
    
    /**
     * The method containing this feature
     */
    private AMethod owner;
    
    /**
     * Annotations on this structure visible after compilation
     */
    private AnAnnotation[] annotations;

    
    public AMethod getOwner() {
        return owner;
    }

    
    public void setOwner(AMethod owner) {
        this.owner = owner;
    }

    
    public AnAnnotation[] getAnnotations() {
        return annotations;
    }

    
    public void setAnnotations(AnAnnotation[] annotations) {
        this.annotations = annotations;
    }
    
}
