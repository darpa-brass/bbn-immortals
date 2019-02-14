package com.securboration.immortals.ontology.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.securboration.immortals.ontology.annotations.etc.Constants;

/**
 * Added to a field to indicate that it should be used as a property. Whether it
 * is a datatype or object property depends upon the field type. Primitive
 * values will have a corresponding XSD type.
 * 
 * @author jstaples
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface RdfProperty {

    /**
     * @return the name of the property. By default, this will be
     *         has[FieldName]. E.g., a field with name "mustard" will result in
     *         a hasMustard property
     */
    public String propertyName() default Constants.UndefinedString;

    /**
     * 
     * @return the domain of the property (i.e., the base class to which the
     *         property can bind). By default, this is the owner class of the
     *         field to which this annotation is bound.
     */
    public Class<?> domain() default Constants.UndefinedClass.class;

    /**
     * 
     * @return the range of the property (i.e., the values of the property). By
     *         default, this is the type of the field to which the annotation
     *         binds.
     */
    public Class<?> range() default Constants.UndefinedClass.class;

}
