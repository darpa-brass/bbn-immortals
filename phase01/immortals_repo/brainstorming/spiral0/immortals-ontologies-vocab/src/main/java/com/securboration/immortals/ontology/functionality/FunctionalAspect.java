package com.securboration.immortals.ontology.functionality;

import com.securboration.immortals.ontology.core.Resource;

/**
 * A description of a specific thing done by some abstraction of functionality.
 * 
 * E.g., a counter abstraction might have increment and zeroize functional
 * aspects
 * 
 * @author Securboration
 *
 */
public class FunctionalAspect {
    
    /**
     * Resources specific to this functional aspect
     */
    private Class<? extends Resource>[] aspectSpecificResourceDependencies;

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

}
