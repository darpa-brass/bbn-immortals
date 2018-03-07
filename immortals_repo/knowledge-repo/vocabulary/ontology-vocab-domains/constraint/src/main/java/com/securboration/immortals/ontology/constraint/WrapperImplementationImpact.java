package com.securboration.immortals.ontology.constraint;

import com.securboration.immortals.ontology.analysis.DataflowNode;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.lang.SourceFile;

public class WrapperImplementationImpact extends AnalysisImpact {
    
    private SourceFile producedSourceFile;
    
    private DataflowNode initializationNode;
    
    private Class<? extends FunctionalAspect> aspectImplemented;

    public Class<? extends FunctionalAspect> getAspectImplemented() {
        return aspectImplemented;
    }

    public void setAspectImplemented(Class<? extends FunctionalAspect> aspectImplemented) {
        this.aspectImplemented = aspectImplemented;
    }

    public DataflowNode getInitializationNode() {
        return initializationNode;
    }

    public void setInitializationNode(DataflowNode initializationNode) {
        this.initializationNode = initializationNode;
    }

    public SourceFile getProducedSourceFile() {
        return producedSourceFile;
    }

    public void setProducedSourceFile(SourceFile producedSourceFile) {
        this.producedSourceFile = producedSourceFile;
    }
}
