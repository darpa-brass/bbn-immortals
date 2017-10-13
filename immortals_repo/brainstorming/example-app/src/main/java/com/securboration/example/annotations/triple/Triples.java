package com.securboration.example.annotations.triple;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An array of triples bound to a code structure 
 * 
 * @author Securboration
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Triples {

    Triple[] value();
}
