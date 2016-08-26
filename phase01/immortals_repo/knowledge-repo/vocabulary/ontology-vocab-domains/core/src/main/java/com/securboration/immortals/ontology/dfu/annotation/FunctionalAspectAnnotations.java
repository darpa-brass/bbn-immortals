package com.securboration.immortals.ontology.dfu.annotation;

import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;

/**
 * A description of a specific thing done by some abstraction of functionality.
 * 
 * E.g., a counter abstraction might have increment and zeroize functional
 * aspects
 * 
 * @author Securboration
 *
 */
@GenerateAnnotation
public class FunctionalAspectAnnotations {
    
    /**
     * The functional aspects
     */
    private FunctionalAspectAnnotation[] aspects;

    
    public FunctionalAspectAnnotation[] getAspects() {
        return aspects;
    }

    
    public void setAspects(FunctionalAspectAnnotation[] aspects) {
        this.aspects = aspects;
    }

}
