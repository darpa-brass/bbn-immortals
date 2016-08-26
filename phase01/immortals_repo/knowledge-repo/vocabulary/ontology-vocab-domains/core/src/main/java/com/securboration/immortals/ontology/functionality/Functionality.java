package com.securboration.immortals.ontology.functionality;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;
import com.securboration.immortals.ontology.property.Property;

/**
 * A description of what a functional unit of software does (but not how it is
 * done). If we know that two different pieces of code do essentially the same
 * thing in a different way, the idea is that we can adapt our software by
 * swapping between them.
 * 
 * @author Securboration
 *
 */
@GenerateAnnotation
public class Functionality {

    /**
     * An ID for the functionality being performed
     */
    private String functionalityId;
    
    /**
     * Our abstraction of functionality comprises various atomic functional 
     * aspects
     */
    private FunctionalAspect[] functionalAspects;
    
    /**
     * Properties that bind to the functionality
     */
    private Property[] functionalityProperties;
    
    /**
     * The resources upon which the functionality depends, if any
     */
    private Class<? extends Resource>[] resourceDependencies;

    public FunctionalAspect[] getFunctionalAspects() {
        return functionalAspects;
    }

    public void setFunctionalAspects(FunctionalAspect[] functionalAspects) {
        this.functionalAspects = functionalAspects;
    }

    public String getFunctionalityId() {
        return functionalityId;
    }

    public void setFunctionalityId(String functionalityId) {
        this.functionalityId = functionalityId;
    }

    
    public Property[] getFunctionalityProperties() {
        return functionalityProperties;
    }

    
    public void setFunctionalityProperties(Property[] functionalityProperties) {
        this.functionalityProperties = functionalityProperties;
    }

    
    public Class<? extends Resource>[] getResourceDependencies() {
        return resourceDependencies;
    }

    
    public void setResourceDependencies(
            Class<? extends Resource>[] resourceDependencies) {
        this.resourceDependencies = resourceDependencies;
    }

}
