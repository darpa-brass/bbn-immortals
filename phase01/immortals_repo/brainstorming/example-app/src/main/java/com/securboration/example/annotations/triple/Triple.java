package com.securboration.example.annotations.triple;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mechanism for specifying a triple that possibly binds to code structure(s)
 * 
 * The ultimate goal is to never require programmers to provide raw triples
 * (instead use higher-level annotations that are friendlier to work with)
 * 
 * @author jstaples
 *
 */
@Repeatable(Triples.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Triple {

    /**
     * If non-empty, all other values in this interface will be ignored
     * 
     * @return a statement containing a subject URI, a predicate URI, and an
     *         object URI
     */
    String value() default "";

    /**
     * If non-empty, the {@link #subjectClass()} value will be ignored
     * 
     * @return the URI of this triple's subject
     */
    String subjectUri() default "";

    /**
     * 
     * @return a UUID for the subject of this triple (which is a code
     *         structure). If empty, the subject is simply the structure to
     *         which the annotation is bound.
     */
    String subjectStructure() default "";

    /**
     * 
     * @return the URI of this triple's predicate
     */
    String predicateUri() default "";

    /**
     * If non-empty, the {@link #objectClass()} value will be ignored
     * 
     * @return the URI of this triple's object
     */
    String objectUri() default "";

    /**
     * 
     * @return the object of this triple (which is a code structure). If empty,
     *         the object is simply the structure to which the annotation is
     *         bound.
     */
    String objectStructure() default "";
}
