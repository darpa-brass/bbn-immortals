package com.securboration.immortals.ontology.pojos.markup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation indicating a class whose instance values are derived from a
 * default constructor
 * 
 * Any properties added by this class will be ignored (only the parent's
 * properties will be included in the resultant triples).
 * 
 * @author Securboration
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConceptInstance {
    /**
     * 
     * @return an id that disambiguates this concept instance from others of the
     * same type
     */
    public long id() default 0l;
}
