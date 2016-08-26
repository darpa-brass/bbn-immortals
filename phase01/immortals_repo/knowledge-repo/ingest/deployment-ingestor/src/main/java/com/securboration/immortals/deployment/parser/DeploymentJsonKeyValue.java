package com.securboration.immortals.deployment.parser;

import org.json.JSONObject;

public class DeploymentJsonKeyValue{
    
    private String key;
    Object value;
    
    public DeploymentJsonKeyValue(){}
    DeploymentJsonKeyValue(JSONObject o){
        
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    } 
}