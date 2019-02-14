package com.securboration.immortals.ontology.bytecode.dependency;

import com.securboration.immortals.ontology.bytecode.Dependency;
import com.securboration.immortals.ontology.bytecode.JarArtifact;

/**
 * Models a dependency relationship between a JAR and those it depends upon
 * 
 * @author Securboration
 *
 */
public class LibraryDependencyEdge {
    
    /**
     * The artifact 
     */
    private JarArtifact artifact;
    
    /**
     * The dependencies of the artifact
     */
    private Dependency[] dependency;
    
    
    public JarArtifact getArtifact() {
        return artifact;
    }
    
    public void setArtifact(JarArtifact artifact) {
        this.artifact = artifact;
    }

    
    public Dependency[] getDependency() {
        return dependency;
    }

    
    public void setDependency(Dependency[] dependency) {
        this.dependency = dependency;
    }

}
