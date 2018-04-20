package com.securboration.immortals.ontology.java.testing.instance;

import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Functionality;

public class ProvidedFunctionalityValidationInstance {
    
    private String[] intents;
    
    private Class<? extends Functionality> functionalityValidated;
    private Class<? extends FunctionalAspect>[] aspectsValidated;
    
    private String methodPointer;
    
    public String[] getIntents() {
        return intents;
    }

    public void setIntents(String[] intents) {
        this.intents = intents;
    }

    public String getMethodPointer() {
        return methodPointer;
    }

    public void setMethodPointer(String methodPointer) {
        this.methodPointer = methodPointer;
    }

    
    public Class<? extends FunctionalAspect>[] getAspectsValidated() {
        return aspectsValidated;
    }

    
    public void setAspectsValidated(
            Class<? extends FunctionalAspect>[] aspectsValidated) {
        this.aspectsValidated = aspectsValidated;
    }

    
    public Class<? extends Functionality> getFunctionalityValidated() {
        return functionalityValidated;
    }

    
    public void setFunctionalityValidated(
            Class<? extends Functionality> functionalityValidated) {
        this.functionalityValidated = functionalityValidated;
    }
}
