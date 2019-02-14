package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.analysis.DataflowEdge;
import com.securboration.immortals.ontology.annotations.RdfsComment;

@RdfsComment("Created as a result of violating a constraint")
public class ConstraintViolation {

    private ProscriptiveCauseEffectAssertion constraint;
    
    private PrescriptiveCauseEffectAssertion mitigationStrategyUtilized;

    private DataflowEdge edgeInViolation;
    
    public ProscriptiveCauseEffectAssertion getConstraint() {
        return constraint;
    }

    public void setConstraint(ProscriptiveCauseEffectAssertion constraint) {
        this.constraint = constraint;
    }
    
    public PrescriptiveCauseEffectAssertion getMitigationStrategyUtilized() {
        return mitigationStrategyUtilized;
    }

    public void setMitigationStrategyUtilized(PrescriptiveCauseEffectAssertion mitigationStrategyUtilized) {
        this.mitigationStrategyUtilized = mitigationStrategyUtilized;
    }

    public DataflowEdge getEdgeInViolation() {
        return edgeInViolation;
    }

    public void setEdgeInViolation(DataflowEdge edgeInViolation) {
        this.edgeInViolation = edgeInViolation;
    }
}
