package com.securboration.immortals.ontology.constraint;

import com.securboration.immortals.ontology.property.Property;

public class PropertyMisMatch {

    private Property property1;
    private Property property2;

    private String property1FieldName;
    private String property2FieldName;

    private Object property1FieldValue;
    private Object property2FieldValue;

    public Property getProperty1() {
        return property1;
    }

    public void setProperty1(Property property1) {
        this.property1 = property1;
    }

    public Property getProperty2() {
        return property2;
    }

    public void setProperty2(Property property2) {
        this.property2 = property2;
    }

    public String getProperty1FieldName() {
        return property1FieldName;
    }

    public void setProperty1FieldName(String property1FieldName) {
        this.property1FieldName = property1FieldName;
    }

    public String getProperty2FieldName() {
        return property2FieldName;
    }

    public void setProperty2FieldName(String property2FieldName) {
        this.property2FieldName = property2FieldName;
    }

    public Object getProperty1FieldValue() {
        return property1FieldValue;
    }

    public void setProperty1FieldValue(Object property1FieldValue) {
        this.property1FieldValue = property1FieldValue;
    }

    public Object getProperty2FieldValue() {
        return property2FieldValue;
    }

    public void setProperty2FieldValue(Object property2FieldValue) {
        this.property2FieldValue = property2FieldValue;
    }
}
