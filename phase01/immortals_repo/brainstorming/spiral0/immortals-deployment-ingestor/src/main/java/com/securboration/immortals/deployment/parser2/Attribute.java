package com.securboration.immortals.deployment.parser2;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class Attribute {
    
    public static Attribute[] getAttributes(JSONObject attributes){
        
        if(JSONObject.getNames(attributes) == null){
            return new Attribute[]{};
        }
        
        List<Attribute> relationships = new ArrayList<>();
        
        for(String attributeName:JSONObject.getNames(attributes)){
            
            //TODO: for now assume all attribute values are Strings
            String attributeValue = 
                    attributes.optString(attributeName,null);
            
            relationships.add(
                    new Attribute(
                            attributeName,
                            attributeValue
                            ));
        }
        
        return relationships.toArray(new Attribute[]{});
    }

    private final String key;
    
    //a String or primitive
    private final Object value;

    public Attribute(String key, Object value) {
        super();
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }
    
}
