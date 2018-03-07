package com.securboration.immortals.ontology.inference;

import java.util.ArrayList;
import java.util.List;

public class InferenceRules{
    
    private final List<InferenceRule> rules = new ArrayList<>();
    
    private Integer maxIterations;
    private Long maxTimeMillis;
    private Boolean iterateUntilNoNewTriples;

    
    public List<InferenceRule> getRules() {
        return rules;
    }


    
    public Integer getMaxIterations() {
        return maxIterations;
    }


    
    public void setMaxIterations(Integer maxIterations) {
        this.maxIterations = maxIterations;
    }


    
    public Long getMaxTimeMillis() {
        return maxTimeMillis;
    }


    
    public void setMaxTimeMillis(Long maxTimeMillis) {
        this.maxTimeMillis = maxTimeMillis;
    }


    
    public Boolean getIterateUntilNoNewTriples() {
        return iterateUntilNoNewTriples;
    }


    
    public void setIterateUntilNoNewTriples(Boolean iterateUntilNoNewTriples) {
        this.iterateUntilNoNewTriples = iterateUntilNoNewTriples;
    }
    
}