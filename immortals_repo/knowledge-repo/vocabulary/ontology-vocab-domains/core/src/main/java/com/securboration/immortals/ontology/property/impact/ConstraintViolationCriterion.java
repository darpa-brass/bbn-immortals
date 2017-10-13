package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.constraint.ConstraintCriterionType;

/**
 * Describes the impact of violating a constraint provided in the form of a 
 * proscriptive assertion
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Describes the impact of violating a constraint provided in the form of" +
    " a  proscriptive assertion  @author jstaples ")
public class ConstraintViolationCriterion extends CriterionStatement {
    
    /**
     * The criterion for the violation
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The criterion for the violation")
    private ConstraintCriterionType triggeringConstraintCriterion;
    
    /**
     * The effect of the violation
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The effect of the violation")
    private ProscriptiveCauseEffectAssertion constraint;
    
    public ConstraintCriterionType getTriggeringConstraintCriterion() {
        return triggeringConstraintCriterion;
    }
    
    public void setTriggeringConstraintCriterion(
            ConstraintCriterionType constraintCriterion) {
        this.triggeringConstraintCriterion = constraintCriterion;
    }
    
    public ProscriptiveCauseEffectAssertion getConstraint() {
        return constraint;
    }
    
    public void setConstraint(ProscriptiveCauseEffectAssertion constraint) {
        this.constraint = constraint;
    }
    

}
