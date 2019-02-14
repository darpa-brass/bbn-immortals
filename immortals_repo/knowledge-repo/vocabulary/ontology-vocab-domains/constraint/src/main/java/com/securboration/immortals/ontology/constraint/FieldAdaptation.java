package com.securboration.immortals.ontology.constraint;

import com.securboration.immortals.ontology.lang.WrapperAdaptation;

public class FieldAdaptation extends WrapperAdaptation {
    
    public FieldAdaptation() {}
    
    public FieldAdaptation(String _fieldType, String _ownerName) {
        fieldType = _fieldType;
        ownerName = _ownerName;
    }
    
    private String fieldType;
    
    private String ownerName;

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
