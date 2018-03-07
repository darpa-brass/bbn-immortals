package com.securboration.immortals.ontology.constraint;

/**
 * An enumeration of criteria that operate on resources. E.g., some assertion
 * might be true only when a given resource is present.
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "An enumeration of criteria that operate on resources. E.g., some" +
    " assertion might be true only when a given resource is present. " +
    " @author jstaples ")
public enum ResourceCriterionType {
    
    WHEN_RESOURCE_PRESENT,
    WHEN_RESOURCE_ABSENT,

    WHEN_RESOURCE_ADDED,
    WHEN_RESOURCE_REMOVED,
    
    WHEN_RESOURCE_ALTERED
    
}
