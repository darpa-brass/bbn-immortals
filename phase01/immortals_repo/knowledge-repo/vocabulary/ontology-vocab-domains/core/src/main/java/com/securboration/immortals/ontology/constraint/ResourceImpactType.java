package com.securboration.immortals.ontology.constraint;

/**
 * An enumeration of impacts on resources. E.g., some condition in the ecosystem
 * may result in the increased consumption of a resource,
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "An enumeration of impacts on resources. E.g., some condition in the" +
    " ecosystem may result in the increased consumption of a resource, " +
    " @author jstaples ")
public enum ResourceImpactType {
    
    INCREASES_CONSUMPTION_OF,
    DECREASES_CONSUMPTION_OF,
    
    ADDS_DEPENDENCY_UPON,
    REMOVES_DEPENDENCY_UPON,
    
    DOES_NOT_AFFECT,
    ;
}
