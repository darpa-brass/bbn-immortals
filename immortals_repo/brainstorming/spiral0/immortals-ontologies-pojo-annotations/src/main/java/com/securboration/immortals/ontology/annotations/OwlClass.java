package com.securboration.immortals.ontology.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An owl:class
 * 
 * @author jstaples
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface OwlClass {

    /**
     * @return the parents of this class. The default value is an empty array.
     *         If any parents are specified, the java type hierarchy will be
     *         ignored when generating subclass-of relationships.
     */
    Class<?>[] parents() default {};

}
