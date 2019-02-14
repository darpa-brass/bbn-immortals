package com.securboration.immortals.bcad.dataflow;

import java.util.UUID;

public class ActionCreate extends Action{
    
    private final String uuid;
    private final String typeDesc;
    private final DataLocation location;
    
    @Override
    public String toString() {
        return String.format(
            "CREATE %s (a %s) at %s", 
            uuid, 
            typeDesc,
            location
            );
    }

    public ActionCreate(String typeDesc, String semanticType, DataLocation location) {
        super();
        this.uuid = UUID.randomUUID().toString();
        this.typeDesc = typeDesc;
        this.location = location;
    }
    

}
