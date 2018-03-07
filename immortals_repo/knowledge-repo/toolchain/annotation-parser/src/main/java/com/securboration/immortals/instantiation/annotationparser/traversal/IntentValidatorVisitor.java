package com.securboration.immortals.instantiation.annotationparser.traversal;

public interface IntentValidatorVisitor {
    
    public void visitMethods(String classHash, byte[] bytes);
}
