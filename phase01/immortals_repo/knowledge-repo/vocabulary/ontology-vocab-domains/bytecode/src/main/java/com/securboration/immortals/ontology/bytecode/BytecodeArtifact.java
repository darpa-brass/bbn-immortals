package com.securboration.immortals.ontology.bytecode;


/**
 * A bytecode compilation artifact
 * 
 * @author Securboration
 *
 */
public class BytecodeArtifact {

    /**
     * The binary form of the artifact. For a naked class, bytecode; for a JAR,
     * a compressed archive of classes; etc.
     */
    private byte[] binaryForm;
    
    /**
     * The classes contained in this artifact
     */
    private AClass[] classes;
    
    /**
     * Identifies this artifact type
     */
    private BytecodeArtifactCoordinate coordinate;

    /**
     * The dependencies of this artifact
     */
    private Dependency[] dependencies;

    public byte[] getBinaryForm() {
        return binaryForm;
    }

    public void setBinaryForm(byte[] binaryForm) {
        this.binaryForm = binaryForm;
    }

    public AClass[] getClasses() {
        return classes;
    }

    public void setClasses(AClass[] classes) {
        this.classes = classes;
    }

    public BytecodeArtifactCoordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(BytecodeArtifactCoordinate coordinate) {
        this.coordinate = coordinate;
    }

    public Dependency[] getDependencies()
    {
        return dependencies;
    }

    public void setDependencies(Dependency[] dependencies)
    {
        this.dependencies = dependencies;
    }
}
