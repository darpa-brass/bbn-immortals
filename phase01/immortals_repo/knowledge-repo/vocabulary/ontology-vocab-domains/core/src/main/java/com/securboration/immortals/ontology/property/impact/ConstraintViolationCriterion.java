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
    private ConstraintCriterionType constraintCriterion;
    
    /**
     * The effect of the violation
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The effect of the violation")
    private ProscriptiveCauseEffectAssertion constraint;
    
    public ConstraintCriterionType getConstraintCriterion() {
        return constraintCriterion;
    }
    
    public void setConstraintCriterion(
            ConstraintCriterionType constraintCriterion) {
        this.constraintCriterion = constraintCriterion;
    }
    
    public ProscriptiveCauseEffectAssertion getConstraint() {
        return constraint;
    }
    
    public void setConstraint(ProscriptiveCauseEffectAssertion constraint) {
        this.constraint = constraint;
    }
    

}
