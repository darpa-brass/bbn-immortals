package com.securboration.immortals.ontology.java.compiler.version;

import com.securboration.immortals.ontology.bytecode.BytecodeVersion;

public class JavaSourceVersion {
    
    private BytecodeVersion targetBytecodeVersion;
    private JavaSourceVersion[] backwardCompatibleWith;
    
    public BytecodeVersion getTargetBytecodeVersion() {
        return targetBytecodeVersion;
    }
    
    public void setTargetBytecodeVersion(BytecodeVersion emittedBytecodeVersion) {
        this.targetBytecodeVersion = emittedBytecodeVersion;
    }
    
    public JavaSourceVersion[] getBackwardCompatibleWith() {
        return backwardCompatibleWith;
    }
    
    public void setBackwardCompatibleWith(
            JavaSourceVersion[] backwardCompatibleWith) {
        this.backwardCompatibleWith = backwardCompatibleWith;
    }

}
