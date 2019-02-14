package com.securboration.immortals.ontology.inference;

public class AskQueryResult extends QueryResult{
    private boolean answer;

    
    public boolean isAnswer() {
        return answer;
    }

    
    public void setAnswer(boolean answer) {
        this.answer = answer;
    }
}