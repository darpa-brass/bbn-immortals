package com.securboration.immortals.ontology.bytecode.application;

import com.securboration.immortals.ontology.bytecode.ClasspathElement;

/**
 * Model of a classpath
 * 
 * @author Securboration
 *
 */
public class Classpath {
    
    /**
     * The elements on the classpath
     */
    private ClasspathElement[] element;

    
    public ClasspathElement[] getElement() {
        return element;
    }

    
    public void setElement(ClasspathElement[] element) {
        this.element = element;
    }

}
