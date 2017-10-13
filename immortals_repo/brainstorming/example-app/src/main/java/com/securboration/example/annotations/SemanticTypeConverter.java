package com.securboration.example.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that identifes a method that converts between semantic types
 * 
 * @author jstaples
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface SemanticTypeConverter {

    /**
     * 
     * @return the input type
     */
    String inputSemanticType() default "file://ontology.immortals.securboration.com/r1.0/Datatypes.owl#Object";

    /**
     * 
     * @return the output type
     */
    String outputSemanticType() default "file://ontology.immortals.securboration.com/r1.0/Datatypes.owl#Object";
}
