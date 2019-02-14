package com.securboration.immortals.swri.eval;

import java.util.LinkedHashMap;
import java.util.Map;

public class EvaluationReport {
    
    private final Map<String,Map<String,Object>> kvs = new LinkedHashMap<>();
    
    public EvaluationReport(){
        
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        for(final String category:kvs.keySet()){
            final Map<String,Object> kvsForCategory = kvs.get(category);
            
            for(final String key:kvsForCategory.keySet()){
                final Object value = kvsForCategory.get(key);
                
                sb.append(String.format("%s, %s, %s\n", category, key, value));
            }
        }
        
        return sb.toString();
    }
    
    
    public void add(
            final String category, 
            final String key, 
            final Object value
            ){
        Map<String,Object> map = kvs.get(category);
        
        if(map == null){
            map = new LinkedHashMap<>();
            kvs.put(category, map);
        }
        
        if(kvs.containsKey(key)){
            throw new RuntimeException("attempted to redefine value for key " + key + " in category " + category);
        }
    }

}
