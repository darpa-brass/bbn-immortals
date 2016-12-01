package com.securboration.immortals.ontology.constraint;


/**
 * An enumeration of the criteria that may be applied to properties. E.g., some
 * assertion may only be true if the property is present; another may be true
 * only if the property was added; etc.
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "An enumeration of the criteria that may be applied to properties." +
    " E.g., some assertion may only be true if the property is present;" +
    " another may be true only if the property was added; etc.  @author" +
    " jstaples ")
public enum PropertyCriterionType {
    
    PROPERTY_PRESENT,
    PROPERTY_ABSENT,

    PROPERTY_GREATER_THAN_EXCLUSIVE,
    PROPERTY_GREATER_THAN_INCLUSIVE,
    PROPERTY_LESS_THAN_EXCLUSIVE,
    PROPERTY_LESS_THAN_INCLUSIVE,
    PROPERTY_EQUALS,
    PROPERTY_NOT_EQUALS,
    
    PROPERTY_ADDED,
    PROPERTY_REMOVED,
    
    PROPERTY_UNCHANGED,
    PROPERTY_CHANGES,
    PROPERTY_INCREASES,
    PROPERTY_DECREASES,
    PROPERTY_MINIMIZED,
    PROPERTY_MAXIMIZED,
    
}
