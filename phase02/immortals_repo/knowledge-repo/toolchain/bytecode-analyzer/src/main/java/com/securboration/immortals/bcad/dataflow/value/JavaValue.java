package com.securboration.immortals.bcad.dataflow.value;

import java.util.UUID;

public abstract class JavaValue {
    
    private final String typeDesc;
    private final String uuid;

    public JavaValue(String typeDesc) {
        this.typeDesc = typeDesc;
        this.uuid = UUID.randomUUID().toString();
    }

    
    public String getTypeDesc() {
        return typeDesc;
    }

    
    public String getUuid() {
        return uuid;
    }

}
