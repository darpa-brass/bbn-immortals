package com.securboration.immortals.ontology.bytecode;

/**
 * A Java ARchive (JAR) artifact
 * 
 * @author Securboration
 *
 */
public class JarArtifact extends BytecodeArtifact {
    
    /**
     * A jar may contain non executable resources (e.g., a configuration file)
     * A jar may contain other nested jars, a relationship captured here
     * A jar may contain 0 or more classes
     */
    private ClasspathElement[] jarContents;
    
    /**
     * Identifies this artifact type
     */
    private BytecodeArtifactCoordinate coordinate;

    
    public ClasspathElement[] getJarContents() {
        return jarContents;
    }
    
    public void setJarContents(ClasspathElement[] jarContents) {
        this.jarContents = jarContents;
    }
    
    public BytecodeArtifactCoordinate getCoordinate() {
        return coordinate;
    }
    
    public void setCoordinate(BytecodeArtifactCoordinate coordinate) {
        this.coordinate = coordinate;
    }
    
}
