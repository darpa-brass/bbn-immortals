package com.securboration.immortals.ontology.resources.constraints;

/**
 * An enumeration of permitted operations
 * @author Securboration
 *
 */
public enum OperatorType {
    
    //booleans
    TRUE,
    FALSE,
    
    //logical unary
    NEGATE_LOGICAL,//e.g., true => false
    
    //math unary
    NEGATE,//e.g., -1 => +1
    
    //enumeration unary
    IS_ONE_OF,//e.g., IS_ONE_OF("VALUE_A",{"VALUE_A","VALUE_B"}) => true
    
    //logical binary
    AND,
    OR,
    EQUAL_LOGICAL,//e.g., true && false => false
    
    //math binary
    DIFFERENCE_OF,
    SUM_OF,
    PRODUCT_OF,
    QUOTIENT_OF,
    
    //comparison
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    EQUAL,
    GREATER_THAN_OR_EQUAL,
    GREATER_THAN,
    
    ;

}
