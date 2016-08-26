package com.securboration.immortals.ontology.algorithm.purpose;

import com.securboration.immortals.ontology.algorithm.AlgorithmSpecificationProperty;

/**
 * Indicates the purpose of the algorithm
 * 
 * @author Securboration
 *
 */
public class AlgorithmPurposeProperty extends AlgorithmSpecificationProperty {
    
    private AlgorithmPurpose purpose;
    
    public AlgorithmPurposeProperty(){}

    public AlgorithmPurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(AlgorithmPurpose purpose) {
        this.purpose = purpose;
    }
    
}
