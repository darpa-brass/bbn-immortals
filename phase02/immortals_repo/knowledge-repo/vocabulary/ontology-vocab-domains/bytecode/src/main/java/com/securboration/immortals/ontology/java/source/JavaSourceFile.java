package com.securboration.immortals.ontology.java.source;

/**
 * A .java file used as part of a build
 * 
 * @author Securboration
 *
 */
public class JavaSourceFile {
    
    /**
     * The contents of the source file encoded as a byte array
     */
    private byte[] bytes;
    
    /**
     * The name of the source file
     */
    private String name;

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
