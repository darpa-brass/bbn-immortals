package com.securboration.immortals.ontology.dfu.annotation;

import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;

/**
 * A wrapper for multiple functional aspects
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A wrapper for multiple functional aspects  @author jstaples ")
@GenerateAnnotation
public class FunctionalAspectAnnotations {
    
    /**
     * The functional aspects
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The functional aspects")
    private FunctionalAspectAnnotation[] aspects;

    
    public FunctionalAspectAnnotation[] getAspects() {
        return aspects;
    }

    
    public void setAspects(FunctionalAspectAnnotation[] aspects) {
        this.aspects = aspects;
    }

}
