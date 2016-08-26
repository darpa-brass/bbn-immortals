package com.securboration.immortals.deployment.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class DeploymentJsonContainment{
        
        private DeploymentJsonKeyValue[] keyValues;

        public DeploymentJsonContainment(){}
        DeploymentJsonContainment(JSONObject obj){
            
            List<DeploymentJsonKeyValue> kvs = 
                    traverseElementsRecursive(obj,null);
            
            keyValues = kvs.toArray(new DeploymentJsonKeyValue[]{});
        }
        
        private List<DeploymentJsonKeyValue> traverseElementsRecursive(
                JSONObject current,
                DeploymentJsonKeyValue currentKeyValue
                ){
            
            if(current == null){
                return new ArrayList<>();
            }
            
//            System.out.printf("looking at a %s\n", current.getClass().getName());
            
            if(JSONObject.getNames(current) == null){
//                System.out.printf("\tit's empty\n");
                currentKeyValue.value = new DeploymentJsonKeyValue();
                return new ArrayList<>();
            }
            
            List<DeploymentJsonKeyValue> keyValues = new ArrayList<>();
            for(String name:JSONObject.getNames(current)){
                
                Object value = current.get(name);
                
                if(value  instanceof JSONObject){
                    //it's a nested structure, recurse
                    
                    DeploymentJsonKeyValue child = new DeploymentJsonKeyValue();
                    keyValues.add(child);
                    child.setKey(name);
                    
//                    System.out.printf("diving into key %s\n", key);
                    
                    traverseElementsRecursive(
                            (JSONObject)value,
                            child);
                } else {
                    System.out.printf("\t%s is a %s\n", name, current.get(name).getClass());
                    
                    throw new RuntimeException("unhandled case");
                }
            }
            
            if(currentKeyValue != null){
                currentKeyValue.value = keyValues.toArray(new DeploymentJsonKeyValue[]{});
            }
            
            return keyValues;
            
        }
        public DeploymentJsonKeyValue[] getKeyValues() {
            return keyValues;
        }
        public void setKeyValues(DeploymentJsonKeyValue[] keyValues) {
            this.keyValues = keyValues;
        }
        
    }