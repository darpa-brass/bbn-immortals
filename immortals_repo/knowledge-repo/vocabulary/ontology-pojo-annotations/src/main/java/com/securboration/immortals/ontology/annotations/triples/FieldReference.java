package com.securboration.immortals.ontology.annotations.triples;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Allows a predicate derived from a POJO to be used in a triple.  For example,
 * the integer field count in class com.example.AClass would be converted into a 
 * datatype property called hasCount that binds to the semantic class AClass.
 * Using this annotation, we could assert the following triple:
 * 
 * hasClass rdfs:subpropertyof IncrementalCounterProperty
 * 
 * @author jstaples
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldReference {

    /**
     * 
     * @return the owner of the field
     */
    Class<?> fieldOwner();
    
    /**
     * The name of the field in the owner's class
     */
    String fieldName();

}
