package com.securboration.immortals.ontology.bytecode;

/**
 * A Java ARchive (JAR) artifact
 * 
 * @author Securboration
 *
 */
public class JarArtifact extends BytecodeArtifact {
    
    /**
     * A jar may contain other nested jars, a relationship captured here
     */
    private JarArtifact[] nestedJars;

    public JarArtifact[] getNestedJars() {
        return nestedJars;
    }

    public void setNestedJars(JarArtifact[] nestedJars) {
        this.nestedJars = nestedJars;
    }
    
}
