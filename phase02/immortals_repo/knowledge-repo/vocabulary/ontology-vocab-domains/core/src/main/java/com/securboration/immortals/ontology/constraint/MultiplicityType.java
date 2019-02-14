package com.securboration.immortals.ontology.constraint;

/**
 * An enumeration of multiplicity bindings. For example, some assertion might
 * apply to all of a set of criteria whereas another assertion might apply to
 * any one of a set of criteria.
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "An enumeration of multiplicity bindings. For example, some assertion" +
    " might apply to all of a set of criteria whereas another assertion" +
    " might apply to any one of a set of criteria.  @author jstaples ")
public enum MultiplicityType {
    
    APPLICABLE_TO_ONE_OF,
    APPLICABLE_TO_ALL_OF,
    APPLICABLE_TO_NONE_OF
    ;
    

}
