package com.securboration.immortals.bcad.dataflow.value;

import java.util.HashMap;
import java.util.Map;

public class JavaValueClass extends JavaValue {
    
    public JavaValueClass(String typeDesc) {
        super(typeDesc);
    }

    private final Map<String,JavaValue> staticFields = new HashMap<>();

    
    public Map<String, JavaValue> getStaticFields() {
        return staticFields;
    }
    
    

}
