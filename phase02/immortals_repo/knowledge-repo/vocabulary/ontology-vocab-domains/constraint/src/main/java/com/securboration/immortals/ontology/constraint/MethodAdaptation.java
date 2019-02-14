package com.securboration.immortals.ontology.constraint;

import com.securboration.immortals.ontology.lang.WrapperAdaptation;

public class MethodAdaptation extends WrapperAdaptation {
    private String[] argTypes;
    private String returnType;
    private String signature;
    
    public String[] getArgTypes() {
        return argTypes;
    }

    public void setArgTypes(String[] argTypes) {
        this.argTypes = argTypes;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
