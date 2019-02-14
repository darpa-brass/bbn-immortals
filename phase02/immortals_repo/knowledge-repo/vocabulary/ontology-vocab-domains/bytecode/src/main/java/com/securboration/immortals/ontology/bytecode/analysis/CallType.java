package com.securboration.immortals.ontology.bytecode.analysis;


/**
 * Enumeration of the various bytecode invocation types
 * 
 * @author jstaples
 *
 */
public enum CallType {
    
    INVOKE_SPECIAL,    /* invoke a special instance method */
    INVOKE_VIRTUAL,    /* invoke an instance method */
    INVOKE_STATIC,     /* invoke a static method */
    INVOKE_INTERFACE,  /* invoke an interface method */
    INVOKE_DYNAMIC,    /* invoke a dynamic call site */
    ;

}
