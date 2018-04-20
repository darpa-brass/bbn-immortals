package com.securboration.immortals.ontology.pattern.spec;


/**
 * The code-level specification for a specific step in an aspect's use.
 * Additionally, in order to differentiate between multiple specs performing
 * the same functionality, the owner of the aspect is specified.
 **/
public class CodeSpec {
    
    private String code;
    
    private String className;
    
    private String methodSignature;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
    }
}
