package com.securboration.immortals.ontology.java.testing.annotation;

import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;

@GenerateAnnotation
public class ProvidedFunctionalityValidationAnnotation {
    
    private String[] intents;

    public String[] getIntents() {
        return intents;
    }

    public void setIntents(String[] intents) {
        this.intents = intents;
    }
}
