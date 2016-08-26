package com.securboration.immortals.deployment.parser;

import org.json.JSONObject;

public class DeploymentTypeDefinition{
    
    private String typeName;
    private String elementType;
    private String[] enumerations;
    
    public DeploymentTypeDefinition(){}
    
    DeploymentTypeDefinition(String name,JSONObject o){
        this.typeName = name;
        this.elementType = o.getString("type");
        
        if(!o.isNull("enum")){
            this.enumerations = DeploymentUmlIngestor.toStringArray(o.getJSONArray("enum"));
        }
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public String[] getEnumerations() {
        return enumerations;
    }

    public void setEnumerations(String[] enumerations) {
        this.enumerations = enumerations;
    }
}