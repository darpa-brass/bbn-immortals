package com.securboration.immortals.semanticweaver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ObjectMapper {
    
    private final Set<Object> objectsToSerialize = new HashSet<>();
    private final Map<Object,String> map = new HashMap<>();
    
    public void registerObjectToSerialize(Object o){
        objectsToSerialize.add(o);
    }
    
    public void registerMapping(Object o, String uri){
        if(map.containsKey(o)){
            final String expected = uri;
            final String actual = map.get(o);
            
            if(!map.get(o).equals(uri)){
                throw new RuntimeException(expected + " != " + actual);
            }
        }
        
        map.put(o, uri);
    }
    
    public String getMapping(Object o){
        return map.get(o);
    }

    
    public Set<Object> getObjectsToSerialize() {
        return objectsToSerialize;
    }

}
