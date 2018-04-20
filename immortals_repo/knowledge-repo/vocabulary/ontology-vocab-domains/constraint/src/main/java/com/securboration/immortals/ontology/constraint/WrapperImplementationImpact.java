package com.securboration.immortals.ontology.constraint;

import com.securboration.immortals.ontology.analysis.DataflowNode;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.lang.SourceFile;

public class WrapperImplementationImpact extends AnalysisImpact {
    
    private SourceFile[] producedSourceFiles;
    
    private SourceFile augmentedUserFile;
    
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

    public SourceFile[] getProducedSourceFiles() {
        return producedSourceFiles;
    }

    public void setProducedSourceFiles(SourceFile[] producedSourceFiles) {
        this.producedSourceFiles = producedSourceFiles;
    }

    public SourceFile getAugmentedUserFile() {
        return augmentedUserFile;
    }

    public void setAugmentedUserFile(SourceFile augmentedUserFile) {
        this.augmentedUserFile = augmentedUserFile;
    }
}
