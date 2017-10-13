package com.securboration.immortals.ontology.inference;


public class InferenceRuleExecution {
    
    private InferenceRule rule;
    
    private QueryResult result;

    
    public InferenceRule getRule() {
        return rule;
    }

    
    public void setRule(InferenceRule rule) {
        this.rule = rule;
    }

    
    public QueryResult getResult() {
        return result;
    }

    
    public void setResult(QueryResult result) {
        this.result = result;
    }

}
