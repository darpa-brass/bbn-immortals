package com.securboration.immortals.ontology.algorithm;

import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;

/**
 * An algorithm for doing something
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "An algorithm for doing something  @author jstaples ")
@GenerateAnnotation
public class Algorithm {

    /**
     * Properties of the algorithm
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Properties of the algorithm")
    private AlgorithmProperty[] properties;
    
    public Algorithm(){}

    public AlgorithmProperty[] getProperties() {
        return properties;
    }

    public void setProperties(AlgorithmProperty[] properties) {
        this.properties = properties;
    }
    
}
