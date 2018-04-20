package com.securboration.immortals.ontology.java.testing.annotation;

import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;

@GenerateAnnotation
public class ProvidedFunctionalityValidationAnnotation {
    
    private String[] intents;
    
    private Class<? extends Functionality> validatedFunctionality;
    private Class<? extends FunctionalAspect>[] validatedAspects;

    public String[] getIntents() {
        return intents;
    }

    public void setIntents(String[] intents) {
        this.intents = intents;
    }

    
    public Class<? extends FunctionalAspect>[] getValidatedAspects() {
        return validatedAspects;
    }

    
    public void setValidatedAspects(
            Class<? extends FunctionalAspect>[] validatedAspects) {
        this.validatedAspects = validatedAspects;
    }

    
    public Class<? extends Functionality> getValidatedFunctionality() {
        return validatedFunctionality;
    }

    
    public void setValidatedFunctionality(
            Class<? extends Functionality> validatedFunctionality) {
        this.validatedFunctionality = validatedFunctionality;
    }
}
