package com.securboration.example.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An identifier for a stateful aspect of a DFU.
 * <p>
 * For example, a DFU abstraction of a counter might have three inseparable
 * functional aspects:
 * <ol>
 * <li>increment (add 1 to the current counter value)</li>
 * <li>reset (set the current counter value to 0)</li>
 * <li>get (retrieve the current counter value)</li>
 * </ol>
 * 
 * Each of the operations above depends on the state of a counter variable,
 * which would be identified by this annotation type.
 * 
 * Functional aspects (in this example the three methods above) is identified
 * using the {@link FunctionalDfuAspect} annotation.
 * 
 * @author jstaples
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface StatefulDfuAspect {

    /**
     * 
     * @return a unique identifier for this instance of a DFU. This identifier
     *         is used to link external content to the annotation model (e.g.,
     *         so an element in an external DSL file can reference a specific
     *         DFU instance).
     */
    String uuid() default "";

    /**
     * 
     * @return a URI for a concept in an ontology
     */
    String statefulAspectUri() default "file://ontology.immortals.securboration.com/r1.0/Functionality.owl#NOP";

    /**
     * 
     * @return a string containing any additional RDF needed to describe the DFU
     */
    String rdf() default "";
}
