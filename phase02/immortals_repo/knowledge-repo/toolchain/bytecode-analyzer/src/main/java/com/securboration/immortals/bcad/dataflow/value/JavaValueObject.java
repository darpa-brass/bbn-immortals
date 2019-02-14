package com.securboration.immortals.bcad.dataflow.value;

import java.util.HashMap;
import java.util.Map;

public class JavaValueObject extends JavaValue {
    
    public JavaValueObject(String typeDesc) {
        super(typeDesc);
    }

    private final Map<String,JavaValue> fields = new HashMap<>();

    
    public Map<String, JavaValue> getFields() {
        return fields;
    }

}
