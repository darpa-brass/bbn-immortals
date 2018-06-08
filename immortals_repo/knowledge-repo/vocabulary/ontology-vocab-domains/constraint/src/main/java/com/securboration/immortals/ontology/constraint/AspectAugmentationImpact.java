package com.securboration.immortals.ontology.constraint;

import com.securboration.immortals.ontology.analysis.DataflowEdge;
import com.securboration.immortals.ontology.analysis.DataflowNode;
import com.securboration.immortals.ontology.dfu.instance.FunctionalAspectInstance;
import com.securboration.immortals.ontology.property.impact.AnalysisImpact;

public class AspectAugmentationImpact extends AnalysisImpact {
    
    private FunctionalAspectInstance aspectInstance;
    
    private DataflowNode augmentationNode;
    
    private AspectAugmentationSpecification specification;
    
    private DataflowEdge[] edgesAffected;
    
    private int lineNumberToInject;
    
    public AspectAugmentationSpecification getSpecification() {
        return specification;
    }

    public void setSpecification(AspectAugmentationSpecification specification) {
        this.specification = specification;
    }

    public DataflowEdge[] getEdgesAffected() {
        return edgesAffected;
    }

    public void setEdgesAffected(DataflowEdge[] edgesAffected) {
        this.edgesAffected = edgesAffected;
    }
    
    public DataflowNode getAugmentationNode() {
        return augmentationNode;
    }

    public void setAugmentationNode(DataflowNode augmentationNode) {
        this.augmentationNode = augmentationNode;
    }

    public int getLineNumberToInject() {
        return lineNumberToInject;
    }

    public void setLineNumberToInject(int lineNumberToInject) {
        this.lineNumberToInject = lineNumberToInject;
    }

    public FunctionalAspectInstance getAspectInstance() {
        return aspectInstance;
    }

    public void setAspectInstance(FunctionalAspectInstance aspectInstance) {
        this.aspectInstance = aspectInstance;
    }
}
