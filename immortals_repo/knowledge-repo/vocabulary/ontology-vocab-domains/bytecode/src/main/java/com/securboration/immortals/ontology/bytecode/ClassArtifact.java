package com.securboration.immortals.ontology.bytecode;

/**
 * A Java ARchive (JAR) artifact
 * 
 * @author Securboration
 *
 */
public class ClassArtifact extends BytecodeArtifact {
    
    private AClass classModel;

    
    public AClass getClassModel() {
        return classModel;
    }

    
    public void setClassModel(AClass classModel) {
        this.classModel = classModel;
    }
    
}
