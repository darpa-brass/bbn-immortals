package com.securboration.immortals.ontology.constraint;

/**
 * An enumeration of impacts on a constraint
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "An enumeration of impacts on a constraint  @author jstaples ")
public enum ConstraintImpactType {
    
    HARD_CONSTRAINT_VIOLATION,
    SOFT_CONSTRAINT_VIOLATION,
    CONSTRAINT_WARNING
    ;
}
