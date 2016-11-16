package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.constraint.ConstraintCriterionType;

public class ConstraintViolationCriterion extends CriterionStatement {
    
    private ConstraintCriterionType constraintCriterion;
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
