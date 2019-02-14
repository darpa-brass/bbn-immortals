package com.securboration.immortals.ontology.constraint;

import com.securboration.immortals.ontology.analysis.DataflowNode;
import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.lang.SourceFile;
import com.securboration.immortals.ontology.property.impact.AnalysisImpact;

public class WrapperImplementationImpact extends AnalysisImpact {

    private SourceFile[] producedSourceFiles;

    private SourceFile augmentedUserFile;

    private DataflowNode initializationNode;

    private String wrapperClassNameShort;

    private BytecodeArtifactCoordinate newDependencies;

    private String[] additionalDependencies;
    
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

    public String[] getAdditionalDependencies() {
        return additionalDependencies;
    }

    public void setAdditionalDependencies(String[] additionalDependencies) {
        this.additionalDependencies = additionalDependencies;
    }

    public String getWrapperClassNameShort() {
        return wrapperClassNameShort;
    }

    public void setWrapperClassNameShort(String wrapperClassNameShort) {
        this.wrapperClassNameShort = wrapperClassNameShort;
    }

    public BytecodeArtifactCoordinate getNewDependencies() {
        return newDependencies;
    }

    public void setNewDependencies(BytecodeArtifactCoordinate newDependencies) {
        this.newDependencies = newDependencies;
    }
}
