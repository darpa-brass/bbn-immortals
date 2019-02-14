package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.constraint.ConstraintImpactType;
import com.securboration.immortals.ontology.constraint.DirectionOfViolationType;
import com.securboration.immortals.ontology.core.Resource;

/**
 * Models the impact of a constraint violation on a resource
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Models the impact of a constraint violation on a resource  @author" +
    " jstaples ")
public class ConstraintViolationImpact extends ImpactStatement {
    
    /**
     * The type of constraint emitted
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The type of constraint emitted")
    private ConstraintImpactType constraintViolationType;
    
    /**
     * The sign of the violation (overshoot/undershoot)
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The sign of the violation (overshoot/undershoot)")
    private DirectionOfViolationType directionOfViolation;
    
    /**
     * The abstract resource impacted
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The abstract resource impacted")
    private Class<? extends Resource> impactedResource;
    
    /**
     * A human readable message describing the constraint violation
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A human readable message describing the constraint violation")
    private String violationMessage;
    
    public ConstraintImpactType getConstraintViolationType() {
        return constraintViolationType;
    }
    
    public void setConstraintViolationType(
            ConstraintImpactType constraintViolationType) {
        this.constraintViolationType = constraintViolationType;
    }
    
    public Class<? extends Resource> getImpactedResource() {
        return impactedResource;
    }
    
    public void setImpactedResource(Class<? extends Resource> impactedResource) {
        this.impactedResource = impactedResource;
    }
    
    public String getViolationMessage() {
        return violationMessage;
    }
    
    public void setViolationMessage(String violationMessage) {
        this.violationMessage = violationMessage;
    }

    
    public DirectionOfViolationType getDirectionOfViolation() {
        return directionOfViolation;
    }

    
    public void setDirectionOfViolation(
            DirectionOfViolationType directionOfViolation) {
        this.directionOfViolation = directionOfViolation;
    }

}
