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
    
    private JarArtifact library;

    
    public JarArtifact getLibrary() {
        return library;
    }

    
    public void setLibrary(JarArtifact library) {
        this.library = library;
    }
    
}
