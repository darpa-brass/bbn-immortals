package com.securboration.immortals.ontology.lang;

public class WrapperSourceFile extends SourceFile {
    
    private WrapperAdaptation[] wrapperAdaptations;
    
    private String[] augmentedMethods;

    public String[] getAugmentedMethods() {
        return augmentedMethods;
    }

    public void setAugmentedMethods(String[] augmentedMethods) {
        this.augmentedMethods = augmentedMethods;
    }

    public WrapperAdaptation[] getWrapperAdaptations() {
        return wrapperAdaptations;
    }

    public void setWrapperAdaptations(WrapperAdaptation[] wrapperAdaptations) {
        this.wrapperAdaptations = wrapperAdaptations;
    }
}
