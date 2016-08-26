package com.securboration.immortals.ontology.bytecode;

/**
 * Model of a java project used to build a bytecode artifact. For now, this is a
 * TODO.
 * 
 * @author Securboration
 *
 */
public class JavaProject {

    /**
     * The source files that are part of the project
     */
    private JavaSourceFile[] sourceFiles;
    
    /**
     * Describes how to build the source files to produce the bytecode artifact
     */
    private BuildMechanism buildMechanism;
    
    /**
     * A coordinate for the artifact that is built
     */
    private BytecodeArtifactCoordinate producesArtifact;

    public JavaSourceFile[] getSourceFiles() {
        return sourceFiles;
    }

    public void setSourceFiles(JavaSourceFile[] sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    public BuildMechanism getBuildMechanism() {
        return buildMechanism;
    }

    public void setBuildMechanism(BuildMechanism buildMechanism) {
        this.buildMechanism = buildMechanism;
    }

    public BytecodeArtifactCoordinate getProducesArtifact() {
        return producesArtifact;
    }

    public void setProducesArtifact(BytecodeArtifactCoordinate producesArtifact) {
        this.producesArtifact = producesArtifact;
    }

}
