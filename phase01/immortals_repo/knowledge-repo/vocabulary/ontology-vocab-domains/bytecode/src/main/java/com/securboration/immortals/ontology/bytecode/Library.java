package com.securboration.immortals.ontology.bytecode;

/**
 * A library is a logical aggregation of compilation artifacts
 * 
 * @author Securboration
 *
 */
public class Library {

    /**
     * The artifacts in the library
     */
    private BytecodeArtifact[] artifacts;

    public BytecodeArtifact[] getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(BytecodeArtifact[] artifacts) {
        this.artifacts = artifacts;
    }
    
    
    
}
