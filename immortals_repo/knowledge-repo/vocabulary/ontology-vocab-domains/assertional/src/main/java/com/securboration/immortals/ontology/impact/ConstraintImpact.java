package com.securboration.immortals.ontology.impact;

import com.securboration.immortals.ontology.impact.constraint.ViolationImpact;
import com.securboration.immortals.ontology.impact.violation.ViolationProvenance;

/**
 * Models the impact of a constraint violation on a resource
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Models the impact of a constraint violation on a resource  @author" +
    " jstaples ")
public class ConstraintImpact extends ImpactStatement {
    
    /**
     * Describes the type of constraint emitted
     */
    private ViolationImpact constraintEmitted;
    
    /**
     * Describes the cause of a constraint
     */
    private ViolationProvenance causeOfViolation;
    
    /**
     * A human-readable description of the violation
     */
    private String violationMessage;

    
    public ViolationImpact getConstraintEmitted() {
        return constraintEmitted;
    }

    
    public void setConstraintEmitted(ViolationImpact constraintEmitted) {
        this.constraintEmitted = constraintEmitted;
    }

    
    public ViolationProvenance getCauseOfViolation() {
        return causeOfViolation;
    }

    
    public void setCauseOfViolation(ViolationProvenance causeOfViolation) {
        this.causeOfViolation = causeOfViolation;
    }

    
    public String getViolationMessage() {
        return violationMessage;
    }

    
    public void setViolationMessage(String violationMessage) {
        this.violationMessage = violationMessage;
    }

}
