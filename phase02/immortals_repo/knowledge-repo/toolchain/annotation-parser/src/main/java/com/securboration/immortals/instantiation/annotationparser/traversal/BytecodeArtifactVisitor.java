package com.securboration.immortals.instantiation.annotationparser.traversal;


public interface BytecodeArtifactVisitor {
    
    public void visitClass(
            final String classHash,
            byte[] bytecode
            );

}
