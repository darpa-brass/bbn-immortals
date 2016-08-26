package com.securboration.immortals.ontology.dfu.annotation;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;
import com.securboration.immortals.ontology.property.Property;

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
public class FunctionalAspectAnnotation {
    
    /**
     * Resources specific to this functional aspect
     */
    private Class<? extends Resource>[] aspectSpecificResourceDependencies;

    /**
     * The functional aspect
     */
    private Class<? extends FunctionalAspect> aspect;
    
    /**
     * The properties associated with the aspect
     */
    private Property[] properties;
    
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

}
