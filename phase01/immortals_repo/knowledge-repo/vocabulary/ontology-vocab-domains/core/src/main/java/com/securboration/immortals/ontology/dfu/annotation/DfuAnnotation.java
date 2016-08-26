package com.securboration.immortals.ontology.dfu.annotation;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;
import com.securboration.immortals.ontology.property.Property;

/**
 * An abstraction that binds what is being done to how it is implemented in
 * bytecode
 * 
 * @author Securboration
 *
 */
@GenerateAnnotation
public class DfuAnnotation {

    /**
     * The functionality performed by the DFU 
     */
    private Class<? extends Functionality> functionalityBeingPerformed;
    
    /**
     * The resources upon which the DFU depends, if any
     */
    private Class<? extends Resource>[] resourceDependencies;
    
    /**
     * The functional aspects of the DFU
     */
    private Class<? extends FunctionalAspect>[] functionalAspects;
    
    /**
     * The properties associated with the DFU
     */
    private Property[] properties;
    
    /**
     * A human-readable tag for durably referencing this DFU.
     */
    private String tag;
    

    public Class<? extends Functionality> getFunctionalityBeingPerformed() {
        return functionalityBeingPerformed;
    }

    public void setFunctionalityBeingPerformed(
            Class<? extends Functionality> functionalityBeingPerformed) {
        this.functionalityBeingPerformed = functionalityBeingPerformed;
    }

    public Class<? extends Resource>[] getResourceDependencies() {
        return resourceDependencies;
    }

    public void setResourceDependencies(
            Class<? extends Resource>[] resourceDependencies) {
        this.resourceDependencies = resourceDependencies;
    }

    public Class<? extends FunctionalAspect>[] getFunctionalAspects() {
        return functionalAspects;
    }

    public void setFunctionalAspects(
            Class<? extends FunctionalAspect>[] functionalAspects) {
        this.functionalAspects = functionalAspects;
    }

    
    public Property[] getProperties() {
        return properties;
    }

    
    public void setProperties(Property[] properties) {
        this.properties = properties;
    }

    
    public String getTag() {
        return tag;
    }

    
    public void setTag(String tag) {
        this.tag = tag;
    }

}
