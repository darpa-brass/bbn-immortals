package com.securboration.immortals.service.eos.api.types;

import java.util.ArrayList;
import java.util.List;

public class SchemaDefinition extends EosType {
    
    private String schemaId;
    private final List<Document> xsds = new ArrayList<>();
    
    public SchemaDefinition(){
        
    }
    
    public SchemaDefinition(String s){
        super(s);
    }

    
    public String getSchemaId() {
        return schemaId;
    }

    
    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    
    public List<Document> getXsds() {
        return xsds;
    }
    
    
    

}
