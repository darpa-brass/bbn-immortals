package com.securboration.immortals.ontology.bytecode.analysis;

/**
 * A bytecode instruction that accesses a field
 * 
 * @author jstaples
 *
 */
public class FieldAccess extends Instruction {
    
    /**
     * The hash of the class that owns the field being accessed
     */
    private String fieldOwnerHash;
    
    /**
     * The name of the field being accessed
     */
    private String fieldName;
    
    /**
     * The desc of the field being accessed
     */
    private String fieldDesc;

    
    public String getFieldOwnerHash() {
        return fieldOwnerHash;
    }

    
    public void setFieldOwnerHash(String fieldOwnerHash) {
        this.fieldOwnerHash = fieldOwnerHash;
    }

    
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
    
    

}
