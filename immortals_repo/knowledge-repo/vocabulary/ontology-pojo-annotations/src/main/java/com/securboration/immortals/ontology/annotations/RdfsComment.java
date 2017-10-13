package com.securboration.immortals.ontology.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An rdfs:comment
 * 
 * @author jstaples
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RdfsComment {

    /**
     * @return the content of an RDFS comment
     */
    String value();
    
}
