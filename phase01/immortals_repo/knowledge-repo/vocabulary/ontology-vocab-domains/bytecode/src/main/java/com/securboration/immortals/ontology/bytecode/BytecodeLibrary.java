package com.securboration.immortals.ontology.bytecode;

import com.securboration.immortals.ontology.core.Resource;

/**
 * A software library
 * 
 * 
 * @author Securboration
 *
 */
public class BytecodeLibrary extends Resource {
    
    private BytecodeArtifactCoordinate library;

    
    protected BytecodeArtifactCoordinate getLibrary() {
        return library;
    }

    
    protected void setLibrary(BytecodeArtifactCoordinate library) {
        this.library = library;
    }
    
}
