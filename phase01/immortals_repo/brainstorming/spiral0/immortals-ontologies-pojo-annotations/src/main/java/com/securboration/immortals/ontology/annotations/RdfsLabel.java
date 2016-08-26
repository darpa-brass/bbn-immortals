package com.securboration.immortals.ontology.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An rdfs:label
 * 
 * @author jstaples
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RdfsLabel {

    /**
     * @return the content of an RDFS label
     */
    String value();
    
}
