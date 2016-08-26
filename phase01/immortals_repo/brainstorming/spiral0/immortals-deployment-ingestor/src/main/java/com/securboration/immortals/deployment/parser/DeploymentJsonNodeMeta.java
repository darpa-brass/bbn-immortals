package com.securboration.immortals.deployment.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class DeploymentJsonNodeMeta{
    
    private DeploymentJsonPointer[] pointers;
    private DeploymentJsonPointer children;
    private DeploymentJsonNodeMetaAspect aspects;
    private DeploymentTypeDefinition[] attributes;
    
    public DeploymentJsonNodeMeta(){}
    DeploymentJsonNodeMeta(JSONObject o){
        
        loadAspects(o);
        loadChildren(o);
        loadPointers(o);
        loadAttributes(o);
    }
    
    private void loadAttributes(JSONObject o){
        if(o.isNull("attributes")){
            return;
        }
        
        o = o.getJSONObject("attributes");
        
        List<DeploymentTypeDefinition> attributes = new ArrayList<>();
        for(String name:JSONObject.getNames(o)){
            attributes.add(
                    new DeploymentTypeDefinition(
                            name,
                            o.getJSONObject(name)));
        }
        
        this.attributes = 
                attributes.toArray(new DeploymentTypeDefinition[]{});
    }
    
    private void loadPointers(JSONObject o){
        
        if(o.isNull("pointers")){
            return;
        }
        
        JSONObject pointers = o.getJSONObject("pointers");
        
        this.pointers = DeploymentUmlIngestor.getPointers(pointers);
    }
    
    private void loadChildren(JSONObject o){
        
        if(o.isNull("children")){
            return;
        }
        
        this.children = DeploymentUmlIngestor.getPointer("children",o.getJSONObject("children"));
    }
    
    
    
    private void loadAspects(JSONObject o){
        
        if(o.isNull("aspects")){
            return;
        }
        
        JSONObject aspect = o.getJSONObject("aspects");
        
        aspects = new DeploymentJsonNodeMetaAspect();
        aspects.instances = DeploymentUmlIngestor.toStringArray(aspect.getJSONArray("instances"));
        aspects.types = DeploymentUmlIngestor.toStringArray(aspect.getJSONArray("types"));
    }
    public DeploymentJsonPointer[] getPointers() {
        return pointers;
    }
    public void setPointers(DeploymentJsonPointer[] pointers) {
        this.pointers = pointers;
    }
    public DeploymentJsonPointer getChildren() {
        return children;
    }
    public void setChildren(DeploymentJsonPointer children) {
        this.children = children;
    }
    public DeploymentJsonNodeMetaAspect getAspects() {
        return aspects;
    }
    public void setAspects(DeploymentJsonNodeMetaAspect aspects) {
        this.aspects = aspects;
    }
    public DeploymentTypeDefinition[] getAttributes() {
        return attributes;
    }
    public void setAttributes(DeploymentTypeDefinition[] attributes) {
        this.attributes = attributes;
    }
    
}