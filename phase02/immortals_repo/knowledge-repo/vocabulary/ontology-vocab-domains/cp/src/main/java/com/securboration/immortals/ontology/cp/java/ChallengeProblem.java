package com.securboration.immortals.ontology.cp.java;

import com.securboration.immortals.ontology.bytecode.BytecodeArtifact;

/**
 * A library is a logical aggregation of compilation artifacts
 * 
 * @author Securboration
 *
 */
public class ChallengeProblem {

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
