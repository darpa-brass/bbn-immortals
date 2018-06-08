package com.securboration.immortals.ontology.constraint;

import com.securboration.immortals.ontology.property.impact.ConstraintViolation;

public class ConstraintAssessmentReport {
    
    private String timeOfAssessment;

    private ConstraintViolation[] constraintViolations;

    public ConstraintViolation[] getConstraintViolations() {
        return constraintViolations;
    }

    public void setConstraintViolations(ConstraintViolation[] constraintViolations) {
        this.constraintViolations = constraintViolations;
    }

    public String getTimeOfAssessment() {
        return timeOfAssessment;
    }

    public void setTimeOfAssessment(String timeOfAssessment) {
        this.timeOfAssessment = timeOfAssessment;
    }
}
