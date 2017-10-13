package com.securboration.immortals.ontology.inference;


public enum SimpleHaltingCondition {
    
    BEFORE_FIRST_ITERATION,//i.e., never runs
    
    AFTER_NO_NEW_TRIPLES,//i.e., until nothing new is discovered
    
    AFTER_ONE_ITERATION,//i.e., after N iterations
    AFTER_TWO_ITERATIONS,
    AFTER_THREE_ITERATIONS,
    AFTER_TEN_ITERATIONS,
    
    NEVER//i.e., never stops running
    ;

}
