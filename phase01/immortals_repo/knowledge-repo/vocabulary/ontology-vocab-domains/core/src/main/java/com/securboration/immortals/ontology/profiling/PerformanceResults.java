package com.securboration.immortals.ontology.profiling;

import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;

@GenerateAnnotation
public class PerformanceResults {

    private PerformanceResult[] results;

    
    public PerformanceResult[] getResults() {
        return results;
    }

    
    public void setResults(PerformanceResult[] results) {
        this.results = results;
    }
    
}
