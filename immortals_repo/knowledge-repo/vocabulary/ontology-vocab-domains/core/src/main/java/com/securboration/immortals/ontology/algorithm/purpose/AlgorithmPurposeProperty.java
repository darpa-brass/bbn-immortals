package com.securboration.immortals.ontology.algorithm.purpose;

import com.securboration.immortals.ontology.algorithm.AlgorithmSpecificationProperty;

/**
 * Indicates the purpose of the algorithm
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Indicates the purpose of the algorithm  @author jstaples ")
public class AlgorithmPurposeProperty extends AlgorithmSpecificationProperty {
    
    /**
     * The algorithm's purpose
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The algorithm's purpose")
    private AlgorithmPurpose purpose;
    
    public AlgorithmPurposeProperty(){}

    public AlgorithmPurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(AlgorithmPurpose purpose) {
        this.purpose = purpose;
    }
    
}
