package com.securboration.immortals.ontology.constraint;

/**
 * An enumeration of the criteria that may apply to values
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "An enumeration of the criteria that may apply to values  @author" +
    " jstaples ")
public enum ValueCriterionType {
    
    VALUE_GREATER_THAN_EXCLUSIVE,
    VALUE_GREATER_THAN_INCLUSIVE,
    
    VALUE_LESS_THAN_EXCLUSIVE,
    VALUE_LESS_THAN_INCLUSIVE,
    
    VALUE_EQUALS
    ;
    

}
