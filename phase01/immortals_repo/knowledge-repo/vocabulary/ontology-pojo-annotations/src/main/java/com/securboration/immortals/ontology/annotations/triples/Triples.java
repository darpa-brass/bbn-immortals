package com.securboration.immortals.ontology.annotations.triples;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An array of triples bound to a code structure 
 * 
 * @author jstaples
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Triples {

    Triple[] value();
}
