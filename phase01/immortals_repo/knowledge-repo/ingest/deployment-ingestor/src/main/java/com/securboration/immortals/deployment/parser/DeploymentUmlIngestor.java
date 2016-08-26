package com.securboration.immortals.deployment.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 
 * 
 * 
 * @author jstaples
 *
 */
public class DeploymentUmlIngestor {
    
    static String[] toStringArray(JSONArray array){
        if(array == null){
            return null;
        }
        
        List<String> values = new ArrayList<>();
        
        for(int i=0;i<array.length();i++){
            values.add((String)array.get(i));
        }
        
        return values.toArray(new String[]{});
    }
    
    private static void verifyArraysSameLength(JSONArray...arrays){
        
        Integer length = null;
        
        for(JSONArray array:arrays){
            if(length == null){
                length = array.length();
            }
            
            if(length != array.length()){
                throw new RuntimeException("assumption violated, array length mismatch");
            }
        }
    }
    
    static DeploymentJsonPointer getPointer(String name, JSONObject o){
        DeploymentJsonPointer p = new DeploymentJsonPointer();
        
        p.pointerName = name;
        
        JSONArray items = o.getJSONArray("items");
        JSONArray minItems = o.getJSONArray("minItems");
        JSONArray maxItems = o.getJSONArray("maxItems");
        
        verifyArraysSameLength(items,minItems,maxItems);
        
        List<DeploymentJsonPointerValue> values = new ArrayList<>();
        
        for(int i=0;i<items.length();i++){
            DeploymentJsonPointerValue pointerValue = 
                    new DeploymentJsonPointerValue();
            
            pointerValue.max = maxItems.getInt(i);
            pointerValue.min = minItems.getInt(i);
            pointerValue.pointerValue = items.getString(i);
            
            values.add(pointerValue);
        }
        
        p.pointerValues = values.toArray(new DeploymentJsonPointerValue[]{});
        
        return p;
    }
    
    static DeploymentJsonPointer[] getPointers(JSONObject o){
        
        if(JSONObject.getNames(o)==null){
            return null;
        }
        
        List<DeploymentJsonPointer> pointers = new ArrayList<>();
        
        for(String name:JSONObject.getNames(o)){
            if(o.isNull(name)){
                continue;
            }
            
            Object pointerObject = o.get(name);
            
            if(pointerObject instanceof String){
                DeploymentJsonPointer p = new DeploymentJsonPointer();
                pointers.add(p);
                
                p.pointerName = name;
                p.pointerValues = new DeploymentJsonPointerValue[]{new DeploymentJsonPointerValue()};
                p.pointerValues[0].pointerValue = (String)pointerObject;
            } else if(pointerObject instanceof JSONObject) {
                pointers.add(getPointer(name,(JSONObject)pointerObject));
            } else {
                throw new RuntimeException("unhandled case for pointer key " + name + ": " + pointerObject.getClass().getName());
            }
        }
        
        return pointers.toArray(new DeploymentJsonPointer[]{});
        
    }
    
    static DeploymentJsonMetaAspectSet[] getSets(JSONObject o){
        List<DeploymentJsonMetaAspectSet> sets = new ArrayList<>();
        
        if(JSONObject.getNames(o) == null){
            return new DeploymentJsonMetaAspectSet[]{};
        }
        
        for(String name:JSONObject.getNames(o)){
            sets.add(
                    new DeploymentJsonMetaAspectSet(
                            name,
                            o.getJSONArray(name)));
        }
        
        return sets.toArray(new DeploymentJsonMetaAspectSet[]{});
    }
    
    static DeploymentJsonRelid[] getRelids(JSONObject o){
        List<DeploymentJsonRelid> relids = new ArrayList<>();
        
        for(String name:JSONObject.getNames(o)){
            DeploymentJsonRelid relid = new DeploymentJsonRelid();
            relids.add(relid);
            
            relid.pointerId = name;
            
            if(!o.isNull(name)){
                relid.tag = o.getString(name);
            }
        }
        
        return relids.toArray(new DeploymentJsonRelid[]{});
    }
    
    public static DeploymentJson ingest(String json){
        JSONObject obj = new JSONObject(json);
        
        return new DeploymentJson(obj);
    }

}
