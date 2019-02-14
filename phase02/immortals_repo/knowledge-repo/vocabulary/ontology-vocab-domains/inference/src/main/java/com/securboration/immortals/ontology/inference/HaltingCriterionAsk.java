package com.securboration.immortals.ontology.inference;


public class HaltingCriterionAsk extends HaltingCriterionComplex {
    
    /**
     * A query that returns true when we should halt
     */
    private AskQuery haltingQuery;

    
    public AskQuery getHaltingQuery() {
        return haltingQuery;
    }

    
    public void setHaltingQuery(AskQuery haltingQuery) {
        this.haltingQuery = haltingQuery;
    }

}
