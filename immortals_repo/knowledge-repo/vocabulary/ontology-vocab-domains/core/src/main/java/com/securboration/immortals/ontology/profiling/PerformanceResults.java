package com.securboration.immortals.ontology.profiling;

import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;

/**
 * A wrapper type for multiple performance results
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A wrapper type for multiple performance results  @author jstaples ")
@GenerateAnnotation
public class PerformanceResults {

    /**
     * The results gathered
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The results gathered")
    private PerformanceResult[] results;

    
    public PerformanceResult[] getResults() {
        return results;
    }

    
    public void setResults(PerformanceResult[] results) {
        this.results = results;
    }
    
}
