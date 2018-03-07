package com.securboration.immortals.ontology.dfu.annotation;

import com.securboration.immortals.ontology.annotations.RdfsComment;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.DesignPattern;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;
import com.securboration.immortals.ontology.property.Property;

/**
 * A description of a specific thing done by some abstraction of functionality.
 * This is a programmer-facing abstraction that allows the definition of new
 * functional signatures.
 * 
 * E.g., a counter abstraction might have init and increment functional aspects.
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A description of a specific thing done by some abstraction of" +
    " functionality. This is a programmer-facing abstraction that allows" +
    " the definition of new functional signatures.  E.g., a counter" +
    " abstraction might have init and increment functional aspects. " +
    " @author jstaples ")
@GenerateAnnotation
public class FunctionalAspectAnnotation implements ResourceDependent {
    
    /**
     * Resources specific to this functional aspect
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Resources specific to this functional aspect")
    private Class<? extends Resource>[] aspectSpecificResourceDependencies;
    
    /**
     * The resources upon which this aspect depends, if any (referenced by URI)
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
            "The resources upon which this aspect depends, if any " +
            "(referenced by URI)")
    private String[] resourceDependencyUris;

    /**
     * The functional aspect
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The functional aspect")
    private Class<? extends FunctionalAspect> aspect;
    
    /**
     * The properties associated with the aspect
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The properties associated with the aspect")
    private Property[] properties;

    /**
     * What design pattern does this implementation follow, e.g. Streaming, block
     */
    @RdfsComment(
            "What design pattern does this implementation follow, e.g. Streaming, block"
    )
    private DesignPattern designPattern;
    
    public Class<? extends FunctionalAspect> getAspect() {
        return aspect;
    }

    public void setAspect(Class<? extends FunctionalAspect> aspect) {
        this.aspect = aspect;
    }

    public Class<? extends Resource>[] getAspectSpecificResourceDependencies() {
        return aspectSpecificResourceDependencies;
    }

    public void setAspectSpecificResourceDependencies(
            Class<? extends Resource>[] aspectSpecificResourceDependencies) {
        this.aspectSpecificResourceDependencies = aspectSpecificResourceDependencies;
    }

    
    public Property[] getProperties() {
        return properties;
    }

    
    public void setProperties(Property[] properties) {
        this.properties = properties;
    }

    @Override
    public String[] getResourceDependencyUris() {
        return resourceDependencyUris;
    }

    
    public void setResourceDependencyUris(String[] resourceDependencyUris) {
        this.resourceDependencyUris = resourceDependencyUris;
    }

    public DesignPattern getDesignPattern() {
        return designPattern;
    }

    public void setDesignPattern(DesignPattern designPattern) {
        this.designPattern = designPattern;
    }
}
