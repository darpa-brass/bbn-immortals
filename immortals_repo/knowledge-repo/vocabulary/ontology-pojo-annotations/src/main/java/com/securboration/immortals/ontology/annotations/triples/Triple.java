package com.securboration.immortals.ontology.annotations.triples;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.securboration.immortals.ontology.annotations.etc.Constants;

/**
 * Structure for specifying a triple that binds to a POJO
 * <p>
 * Triples can be defined as a naked {s,p,o} tuple of URIS (see
 * {@link #nakedTriple}). Alternatively, they can be defined piecewise.
 * <p>
 * For piecewise defined triples, several mechanisms are supported for providing
 * subjects, predicates, and objects:
 * 
 * <ol>
 * <li>The raw URI can be provided as a String. See {@link #subjectUri},
 * {@link #predicateUri}, {@link #objectUri}.</li>
 * <li>A reference to a Java Class in the com.securboration.immortals.ontology
 * package ({@link #subjectClass}, {@link #objectClass}) or to a Class +
 * fieldName pair ({@link #subjectField}, {@link #predicateField},
 * {@link #objectField}). These class references will be automatically converted
 * to URIs when the annotation is analyzed</li>
 * <li>Objects can be string encoded literal values</li>
 * </ol>
 * 
 * <p>
 * If this annotation appears on POJO type com.example.AClass and none of the
 * above mechanisms are used to specify a subject or object, the semantic class
 * derived from com.example.AClass will be used as the subject or object. If no
 * predicate is provided, an exception will be thrown.
 * <p>
 * If this annotation appears on field <i>name</i> in POJO type
 * com.example.AClass and none of the above mechanisms are used to specify a
 * subject or object, the property derived from <i>name</i> will be used as the
 * subject or object. If no predicate is provided, an exception will be thrown.
 * <p>
 * <p>
 * <b>TODO</b>: deal with complex triples. Example: we have the following
 * triple:<br>
 * {s1,p1,{{s3,p3,o3},p2,o2}}<br>
 * This can be flattened into the following array of three simple triples:<br>
 * {s1,p1,$T2}<br>
 * $T2:{$T3,p2,o2}<br>
 * $T3:{s3,p3,o3}<br>
 * The first triple is anonymous and the second two are named. Flattening/
 * naming is required because annotated structures cannot be recursive.
 * 
 * @author jstaples
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Triple {
    
    /**
     * 
     * @return an ID for this triple.  This is useful when one triple is used
     * as the subject, predicate, or object of another or when anonymization is
     * needed.  IDs should be unambiguous within an {@link Triples} construct.
     */
    String id() default "";

    /**
     * Mechanism 1 for defining a triple: provide the raw URIs to use for the
     * subject predicate object tuple. If non-empty, all other values in this
     * interface will be ignored. The other mechanism is to define the triples
     * piecewise by subject, predicate, and object.
     * 
     * @return an array of length 3 containing a subject URI, a predicate URI,
     *         and an object URI
     */
    String[] nakedTriple() default {};

    // Subject. Choose one of the following mechanisms:
    
    /**
     * If non-empty, the predicate and object of this triple will be linked to
     * an anonymous node with the id indicated and any {@link #subjectUri()},
     * {@link #subjectField()}, or {@link #subjectClass()} values will be
     * ignored.
     * 
     * @return an ID for an anonymous subject. IDs should be unambiguous within
     *         an {@link Triples} construct.
     */
    String subjectId() default "";

    /**
     * If non-empty, any {@link #subjectClass()} or {@link #subjectField()}
     * values will be ignored and the predicate/object of this triple will be
     * linked to the indicated URI.
     * 
     * @return the URI of this triple's subject
     */
    String subjectUri() default "";

    /**
     * If non-empty, any {@link #subjectField()} value will be ignored and the
     * predicate/object of this triple will be linked to a URI representing the
     * concept embodied by the indicated class.
     * 
     * @return a class (representing a semantic class) to use as the subject of
     *         the triple
     */
    Class<?> subjectClass() default Constants.UndefinedClass.class;
    
    /**
     * 
     * @return a field (representing a property) to use as the subject of a
     *         triple
     */
    FieldReference subjectField() default @FieldReference(
            fieldName = "", 
            fieldOwner = Constants.UndefinedClass.class
            );

    // Predicate. Choose one of the following mechanisms:

    /**
     * If non-empty, any {@link #predicateField()} value will be ignored and the
     * predicate of this triple will be the indicated URI
     * 
     * @return the URI of this triple's predicate
     */
    String predicateUri() default "";

    /**
     * 
     * @return a field (representing a property) to use as the predicate of a
     *         triple
     */
    FieldReference predicateField() default @FieldReference(
            fieldName = "", 
            fieldOwner = Constants.UndefinedClass.class
            );

    // Object. Choose one of the following mechanisms:
    
    /**
     * If non-empty, the object of this triple will be linked to an anonymous
     * node with the id indicated and any {@link #objectUri()},
     * {@link #objectField()}, {@link #objectClass()}, or
     * {@link #objectLiteral()} values will be ignored.
     * 
     * @return an ID for an anonymous node to use as an object of this triple.
     *         IDs should be unambiguous within an {@link Triples} construct.
     */
    String objectId() default "";

    /**
     * If non-empty, the object of this triple will be linked to the indicated
     * URI and any {@link #objectField()}, {@link #objectClass()}, or
     * {@link #objectLiteral()} values will be ignored
     * 
     * @return the URI of this triple's object
     */
    String objectUri() default "";

    /**
     * If non-empty, the object of this triple will be linked to the concept
     * embodied by the indicated class and any {@link #objectField()} or
     * {@link #objectLiteral()} value will be ignored
     * 
     * @return a class to use as the object of the triple
     */
    Class<?> objectClass() default Constants.UndefinedClass.class;
    
    /**
     * If non-empty, the object of this triple will be linked to the indicated
     * field and any {@link #objectLiteral()} value will be ignored
     * 
     * @return a field (representing a property) to use as the object of a
     *         triple
     */
    FieldReference objectField() default @FieldReference(
            fieldName = "", 
            fieldOwner = Constants.UndefinedClass.class
            );
    
    /**
     * 
     * @return a literal value to use as the object of the triple
     */
    Literal objectLiteral() default @Literal(value = "");

}
