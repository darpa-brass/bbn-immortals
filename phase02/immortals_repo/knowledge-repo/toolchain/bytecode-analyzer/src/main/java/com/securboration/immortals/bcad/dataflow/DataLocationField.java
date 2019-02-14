package com.securboration.immortals.bcad.dataflow;

import com.securboration.immortals.bcad.dataflow.value.JavaValueObject;

public class DataLocationField extends DataLocation {
    
    private final String ownerClass;
    
    private final JavaValueObject instance;
    
    private final String fieldName;

    @Override
    public String toString() {
        
        final String type = instance == null ? "static" : "instance";
        
        final String suffix = instance == null ? "" : instance.toString();
        
        return type + " field " + ownerClass + "." + fieldName + " " + suffix;
    }

    public DataLocationField(
            String ownerClass,
            String fieldName,
            JavaValueObject instance
            ) {
        this.ownerClass = ownerClass;
        this.instance = instance;
        this.fieldName = fieldName;
    }

}
