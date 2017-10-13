package com.securboration.immortals.ontology.core;

/**
 * An enumeration of possible constraint types on the validity of an assertion.
 * E.g., some assertion might never be true whereas another would usually be 
 * true.
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "An enumeration of possible constraint types on the validity of an" +
    " assertion. E.g., some assertion might never be true whereas another" +
    " would usually be  true.  @author jstaples ")
public enum TruthConstraint {

    NONE,
    
    NEVER_TRUE,
    USUALLY_NOT_TRUE,
    
    SOMETIMES_TRUE,
    
    USUALLY_TRUE,
    ALWAYS_TRUE
    ;
    
}
