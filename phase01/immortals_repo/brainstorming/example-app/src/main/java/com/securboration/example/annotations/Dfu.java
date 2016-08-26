package com.securboration.example.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Discrete Functional Unit (DFU) annotation. A DFU is the smallest atomic
 * code unit into which some functionality can be sensibly decomposed for the
 * purposes of reuse and adaptation.
 * <p>
 * The simplest DFU is a stateless function operating on inputs to produce
 * outputs. Such a DFU can be fully described using this annotation alone. In
 * practice, functionality in real-world software is typically aggregated into
 * inseparable, stateful code constructs (for example, classes contain methods
 * that depend on fields which may have been initialized before being invoked).
 * Such constructs, when present, are further disambiguated using the
 * {@link FunctionalDfuAspect} and {@link StatefulDfuAspect} classes.
 * <p>
 * Bindings: this annotation may bind to classes (for DFUs with functional
 * and/or stateful aspects) or directly to methods (for DFUs that do not have
 * functional or stateful aspects).
 * 
 * @author jstaples
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Dfu {

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
    String functionalityUri() default "file://ontology.immortals.securboration.com/r1.0/Functionality.owl#NOP";

    /**
     * 
     * @return a string containing any additional RDF needed to describe the DFU
     */
    String rdf() default "";
}
