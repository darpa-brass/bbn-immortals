package com.securboration.immortals.ontology.lang;

public class AugmentedUserSourceFile extends SourceFile {
    
    private String fileName;
    
    private AugmentedMethodInvocation[] augmentedMethodInvocations;

    public AugmentedMethodInvocation[] getAugmentedMethodInvocations() {
        return augmentedMethodInvocations;
    }

    public void setAugmentedMethodInvocations(AugmentedMethodInvocation[] augmentedMethodInvocations) {
        this.augmentedMethodInvocations = augmentedMethodInvocations;
    }
    
}
