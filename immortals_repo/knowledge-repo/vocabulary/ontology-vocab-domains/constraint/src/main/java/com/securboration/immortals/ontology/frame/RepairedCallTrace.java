package com.securboration.immortals.ontology.frame;

import com.securboration.immortals.ontology.constraint.AspectAugmentationImpact;

public class RepairedCallTrace extends CallTrace {
    
    public RepairedCallTrace(CallTrace callTrace) {
        super(callTrace);
    }
    
    public RepairedCallTrace() {
        super();
    }
    
    private AspectAugmentationImpact[] aspectAugmentationImpacts;

    public AspectAugmentationImpact[] getAspectAugmentationImpacts() {
        return aspectAugmentationImpacts;
    }

    public void setAspectAugmentationImpacts(AspectAugmentationImpact[] aspectAugmentationImpacts) {
        this.aspectAugmentationImpacts = aspectAugmentationImpacts;
    }
}
