package com.securboration.immortals.ontology.constraint;

import com.securboration.immortals.ontology.lang.WrapperAdaptation;

public class FieldAdaptation extends WrapperAdaptation {
    private String fieldType;

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }
}
