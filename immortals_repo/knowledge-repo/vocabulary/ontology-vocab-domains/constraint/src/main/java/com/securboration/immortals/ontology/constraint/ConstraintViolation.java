package com.securboration.immortals.ontology.constraint;

import com.securboration.immortals.ontology.property.impact.PrescriptiveCauseEffectAssertion;
import com.securboration.immortals.ontology.property.impact.ProscriptiveCauseEffectAssertion;

public class ConstraintViolation {

    private ProscriptiveCauseEffectAssertion constraint;
    
    private ScopeOfRepairs scopeOfRepairs;
    
    private PrescriptiveCauseEffectAssertion mitigationStrategyUtilized;
    
    private AnalysisImpact[] analysisImpacts;
    
    public ProscriptiveCauseEffectAssertion getConstraint() {
        return constraint;
    }

    public void setConstraint(ProscriptiveCauseEffectAssertion constraint) {
        this.constraint = constraint;
    }

    public ScopeOfRepairs getScopeOfRepairs() {
        return scopeOfRepairs;
    }

    public void setScopeOfRepairs(ScopeOfRepairs scopeOfRepairs) {
        this.scopeOfRepairs = scopeOfRepairs;
    }

    public PrescriptiveCauseEffectAssertion getMitigationStrategyUtilized() {
        return mitigationStrategyUtilized;
    }

    public void setMitigationStrategyUtilized(PrescriptiveCauseEffectAssertion mitigationStrategyUtilized) {
        this.mitigationStrategyUtilized = mitigationStrategyUtilized;
    }

    public AnalysisImpact[] getAnalysisImpacts() {
        return analysisImpacts;
    }

    public void setAnalysisImpacts(AnalysisImpact[] analysisImpacts) {
        this.analysisImpacts = analysisImpacts;
    }
}
