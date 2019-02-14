package com.securboration.immortals.ontology.constraint;

import com.securboration.immortals.ontology.lang.WrapperAdaptation;

public class MethodAdaptation extends WrapperAdaptation {
    
    private String[] argTypes;
    private String returnType;
    private String signature;
    
    public MethodAdaptation() {}
    
    public MethodAdaptation(String _methodSig) {
        //TODO infer from given signature
        argTypes = null;
        //TODO infer from given signature
        returnType = null;
        signature = _methodSig;
    }
    
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
