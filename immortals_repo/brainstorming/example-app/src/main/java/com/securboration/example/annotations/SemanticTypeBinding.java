package com.securboration.example.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies an equivalent semantic type to a Java code type
 * 
 * @author jstaples
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.TYPE_USE, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE })
public @interface SemanticTypeBinding {

    /**
     * 
     * @return the corresponding semantic type to which the annotated class is
     *         linked
     */
    String semanticType() default "file://ontology.immortals.securboration.com/r1.0/Datatypes.owl#Object";
}
