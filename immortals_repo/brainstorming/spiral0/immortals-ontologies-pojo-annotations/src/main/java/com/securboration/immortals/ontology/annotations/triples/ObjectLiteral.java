package com.securboration.immortals.ontology.annotations.triples;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 
 * @author jstaples
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectLiteral {

    /**
     * 
     * @return a string representing an untyped literal value.  E.g., "13".  
     * 
     * @see <a href="https://jena.apache.org/documentation/notes/typed-literals.html">typed-literals</a>
     */
    String objectLiteral();
    
    /**
     * 
     * @return the type of the literal provided as a string.  E.g., "xsd:int"
     * 
     * @see <a href="https://jena.apache.org/documentation/notes/typed-literals.html">typed-literals</a>
     */
    String objectLiteralType();

}
