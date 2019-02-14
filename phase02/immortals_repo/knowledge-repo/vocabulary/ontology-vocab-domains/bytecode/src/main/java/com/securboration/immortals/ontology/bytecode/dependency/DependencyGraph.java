package com.securboration.immortals.ontology.bytecode.dependency;

/**
 * Models a dependency relationship between one JAR and another
 * 
 * @author Securboration
 *
 */
public class DependencyGraph {
    
    /**
     * A flattened list of dependency edges
     */
    private LibraryDependencyEdge[] dependencyEdge;

    
    public LibraryDependencyEdge[] getDependencyEdge() {
        return dependencyEdge;
    }

    
    public void setDependencyEdge(LibraryDependencyEdge[] dependencyEdge) {
        this.dependencyEdge = dependencyEdge;
    }

}
