package com.securboration.immortals.ontology.constraint;

/**
 * An enumeration of impact types for properties
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "An enumeration of impact types for properties  @author jstaples ")
public enum PropertyImpactType {
    
    ADDS,
    REMOVES,
    
    PROPERTY_INCREASES,
    PROPERTY_DECREASES,
    
    DOES_NOT_AFFECT,
    
    ;
}
