package com.securboration.immortals.deployment.parser;

import org.json.JSONObject;

public class DeploymentJsonRoot{
    
    private String path;
    private String guid;
    
    public DeploymentJsonRoot(){}
    DeploymentJsonRoot(JSONObject obj){
        path = obj.getString("path");
        guid = obj.getString("guid");
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getGuid() {
        return guid;
    }
    public void setGuid(String guid) {
        this.guid = guid;
    }
    
}