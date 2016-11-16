package com.securboration.immortals.ontology.bytecode;


/**
 * A model of something that can reside on a classpath
 * 
 * @author Securboration
 *
 */
public class ClasspathElement {
    
    /**
     * The name of the element
     */
    private String name;
    
    /**
     * A cryptographic hash of the binary form
     */
    private String hash;
    
    /**
     * The binary form of the element. For a naked class, bytecode; for a JAR,
     * a compressed archive of classes; for a resource, its binary form; etc.
     */
    private byte[] binaryForm;

    
    public String getName() {
        return name;
    }

    
    public void setName(String name) {
        this.name = name;
    }

    
    public byte[] getBinaryForm() {
        return binaryForm;
    }

    
    public void setBinaryForm(byte[] binaryForm) {
        this.binaryForm = binaryForm;
    }


    
    public String getHash() {
        return hash;
    }


    
    public void setHash(String hash) {
        this.hash = hash;
    }

}
