package com.securboration.immortals.instantiation;

import java.util.LinkedHashMap;
import java.util.Map;

public class Defaults {
    private static Map<Class<?>,String> defaultValueMap = new LinkedHashMap<>();
    
    static{
        defaultValueMap.put(Class.class, Class.class.getName());
        
        defaultValueMap.put(String.class, "");
        
        defaultValueMap.put(int.class, ""+Integer.MIN_VALUE);
        defaultValueMap.put(Integer.class, ""+Integer.MIN_VALUE);
        
        defaultValueMap.put(float.class, ""+Float.MIN_VALUE+"F");
        defaultValueMap.put(Float.class, ""+Float.MIN_VALUE+"F");
        
        defaultValueMap.put(long.class, ""+Long.MIN_VALUE+"L");
        defaultValueMap.put(Long.class, ""+Long.MIN_VALUE+"L");
        
        defaultValueMap.put(double.class, ""+Double.MIN_VALUE+"D");
        defaultValueMap.put(Double.class, ""+Double.MIN_VALUE+"D");
        
        defaultValueMap.put(boolean.class, ""+false);
        defaultValueMap.put(Boolean.class, ""+false);
        
        defaultValueMap.put(byte.class, ""+Byte.MIN_VALUE);
        defaultValueMap.put(Byte.class, ""+Byte.MIN_VALUE);
        
        defaultValueMap.put(short.class, ""+Short.MIN_VALUE);
        defaultValueMap.put(Short.class, ""+Short.MIN_VALUE);
        
        defaultValueMap.put(char.class, ""+Character.MIN_VALUE);
        defaultValueMap.put(Character.class, ""+Character.MIN_VALUE);
    }
    
    public String getDefaultValueString(Class<?> c){
        if(c.isArray()){
            return "{}";
        }
        
        return defaultValueMap.get(c);
    }
}
