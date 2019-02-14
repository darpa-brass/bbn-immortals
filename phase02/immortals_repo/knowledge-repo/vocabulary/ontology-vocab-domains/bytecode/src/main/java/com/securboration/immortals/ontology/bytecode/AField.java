package com.securboration.immortals.ontology.bytecode;

/**
 * A field in a class
 * 
 * @author Securboration
 *
 */
public class AField extends ClassStructure {
    
    /**
     * A unique identifier for this object
     */
    private String bytecodePointer;
    
    /**
     * The name of the field specified by the programmer. Along with the field
     * descriptor, this is a uniquely identifying tuple for the field in its
     * parent class
     */
    private String fieldName;

    /**
     * The type descriptor of the field. Along with the name, this is a uniquely
     * identifying tuple for the field in its parent class
     */
    private String fieldDesc;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldDesc() {
        return fieldDesc;
    }

    public void setFieldDesc(String fieldDesc) {
        this.fieldDesc = fieldDesc;
    }

    public String getBytecodePointer() {
        return bytecodePointer;
    }

    public void setBytecodePointer(String bytecodePointer) {
        this.bytecodePointer = bytecodePointer;
    }
}
