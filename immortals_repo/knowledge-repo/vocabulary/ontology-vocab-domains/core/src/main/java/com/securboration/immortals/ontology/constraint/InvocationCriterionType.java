package com.securboration.immortals.ontology.constraint;

/**
 * An enumeration of criteria related to the invocation of a method. E.g., some
 * statement might only be true after invocation whereas another might only be
 * true during invocation.
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "An enumeration of criteria related to the invocation of a method." +
    " E.g., some statement might only be true after invocation whereas" +
    " another might only be true during invocation.  @author jstaples ")
public enum InvocationCriterionType {
    
    BEFORE_INVOKING,
    DURING_INVOCATION,
    AFTER_INVOKING,
    
}
