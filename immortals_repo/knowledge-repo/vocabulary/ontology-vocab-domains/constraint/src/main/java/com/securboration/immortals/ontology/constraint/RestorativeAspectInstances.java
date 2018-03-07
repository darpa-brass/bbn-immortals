package com.securboration.immortals.ontology.constraint;

import com.securboration.immortals.ontology.analysis.DataflowNode;
import com.securboration.immortals.ontology.dfu.instance.FunctionalAspectInstance;

public class RestorativeAspectInstances {

    private FunctionalAspectInstance[] aspectInstances;

    private AspectInstanceInjectionSpecification injectionSpecification;
    
    private DataflowNode nodeToInjectAfter;

    public FunctionalAspectInstance[] getAspectInstances() {
        return aspectInstances;
    }

    public void setAspectInstances(FunctionalAspectInstance[] aspectInstances) {
        this.aspectInstances = aspectInstances;
    }

    public AspectInstanceInjectionSpecification getInjectionSpecification() {
        return injectionSpecification;
    }

    public void setInjectionSpecification(AspectInstanceInjectionSpecification injectionSpecification) {
        this.injectionSpecification = injectionSpecification;
    }

    public DataflowNode getNodeToInjectAfter() {
        return nodeToInjectAfter;
    }

    public void setNodeToInjectAfter(DataflowNode nodeToInjectAfter) {
        this.nodeToInjectAfter = nodeToInjectAfter;
    }
}
