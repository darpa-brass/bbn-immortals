package com.securboration.immortals.ontology.algorithm;

import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;

/**
 * An algorithm for doing something
 * 
 * @author Securboration
 *
 */
@GenerateAnnotation
public class Algorithm {

    private AlgorithmProperty[] properties;
    
    public Algorithm(){}

    public AlgorithmProperty[] getProperties() {
        return properties;
    }

    public void setProperties(AlgorithmProperty[] properties) {
        this.properties = properties;
    }
    
}
