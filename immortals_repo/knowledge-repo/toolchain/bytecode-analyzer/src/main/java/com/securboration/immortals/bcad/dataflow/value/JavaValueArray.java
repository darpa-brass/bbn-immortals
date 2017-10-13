package com.securboration.immortals.bcad.dataflow.value;

import java.util.ArrayList;
import java.util.List;

public class JavaValueArray extends JavaValueObject {

    public JavaValueArray(String typeDesc) {
        super(typeDesc);
    }

    private final List<JavaValue> elements = new ArrayList<>();
    
}
