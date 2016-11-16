package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.constraint.ConstraintImpactType;
import com.securboration.immortals.ontology.constraint.DirectionOfViolationType;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Models a constraint violation on a resource"
        )
    )
public class ConstraintViolationImpact extends ImpactStatement {
    
    private ConstraintImpactType constraintViolationType;
    private DirectionOfViolationType directionOfViolation;
    private Class<? extends Resource> impactedResource;
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
