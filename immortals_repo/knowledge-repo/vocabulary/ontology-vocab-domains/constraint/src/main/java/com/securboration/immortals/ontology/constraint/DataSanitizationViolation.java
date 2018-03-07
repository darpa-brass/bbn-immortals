package com.securboration.immortals.ontology.constraint;

import com.securboration.immortals.ontology.functionality.datatype.DataType;

public class DataSanitizationViolation {

    private Class<? extends DataType> mishandledDataType;

    private PropertyMisMatch[] propertyMisMatches;

    public Class<? extends DataType> getMishandledDataType() {
        return mishandledDataType;
    }

    public void setMishandledDataType(Class<? extends DataType> mishandledDataType) {
        this.mishandledDataType = mishandledDataType;
    }

    public PropertyMisMatch[] getPropertyMisMatches() {
        return propertyMisMatches;
    }

    public void setPropertyMisMatches(PropertyMisMatch[] propertyMisMatches) {
        this.propertyMisMatches = propertyMisMatches;
    }
}