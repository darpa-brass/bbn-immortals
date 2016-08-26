package com.securboration.immortals.ontology.functionality;

import com.securboration.immortals.ontology.core.Resource;
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
public class FunctionalAspect {
    
    /**
     * An ID for this functional aspect
     */
    private String aspectId;
    
    /**
     * Resources specific to this functional aspect
     */
    private Class<? extends Resource>[] aspectSpecificResourceDependencies;
    
    /**
     * Identifies an inverse transformation
     */
    private Class<? extends FunctionalAspect> inverseAspect;
    
    /**
     * Properties applicable to this aspect
     */
    private Property[] aspectProperties;

    /**
     * The inputs provided to the functionality abstraction. These can be either
     * explicit (arguments) or implicit (e.g., field accesses)
     */
    private Input[] inputs;

    /**
     * The outputs provided by the functionality abstraction. These can be
     * either explicit (return value) or implicit (locations written)
     */
    private Output[] outputs;

    public Input[] getInputs() {
        return inputs;
    }

    public void setInputs(Input[] inputs) {
        this.inputs = inputs;
    }

    public Output[] getOutputs() {
        return outputs;
    }

    public void setOutputs(Output[] outputs) {
        this.outputs = outputs;
    }

    public Class<? extends Resource>[] getAspectSpecificResourceDependencies() {
        return aspectSpecificResourceDependencies;
    }

    public void setAspectSpecificResourceDependencies(
            Class<? extends Resource>[] aspectSpecificResourceDependencies) {
        this.aspectSpecificResourceDependencies = aspectSpecificResourceDependencies;
    }

    public String getAspectId() {
        return aspectId;
    }

    public void setAspectId(String aspectId) {
        this.aspectId = aspectId;
    }

    
    public Class<? extends FunctionalAspect> getInverseAspect() {
        return inverseAspect;
    }

    
    public void setInverseAspect(Class<? extends FunctionalAspect> inverseAspect) {
        this.inverseAspect = inverseAspect;
    }

    
    public Property[] getAspectProperties() {
        return aspectProperties;
    }

    
    public void setAspectProperties(Property[] aspectProperties) {
        this.aspectProperties = aspectProperties;
    }

}
