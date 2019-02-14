package com.securboration.immortals.ontology.property.impact;

public class PrescriptiveBindingInstance {
    
    private PrescriptiveCauseEffectAssertion mitigationStrategy;
    
    private AssertionBindingSite bindingSite;

    public PrescriptiveCauseEffectAssertion getMitigationStrategy() {
        return mitigationStrategy;
    }

    public void setMitigationStrategy(PrescriptiveCauseEffectAssertion mitigationStrategy) {
        this.mitigationStrategy = mitigationStrategy;
    }

    public AssertionBindingSite getBindingSite() {
        return bindingSite;
    }

    public void setBindingSite(AssertionBindingSite bindingSite) {
        this.bindingSite = bindingSite;
    }
}
