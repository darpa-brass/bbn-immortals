package com.securboration.immortals.annotations.generator;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public class Defaults {
    private static final Map<String,String> defaultValueMap = 
            new LinkedHashMap<>();
    
    static{
        defaultValueMap.put(String.class.getName(), "\"\"");
        
        defaultValueMap.put(int.class.getName(), ""+Integer.MIN_VALUE);
        defaultValueMap.put(Integer.class.getName(), ""+Integer.MIN_VALUE);
        
        defaultValueMap.put(float.class.getName(), ""+Float.MIN_VALUE+"F");
        defaultValueMap.put(Float.class.getName(), ""+Float.MIN_VALUE+"F");
        
        defaultValueMap.put(long.class.getName(), ""+Long.MIN_VALUE+"L");
        defaultValueMap.put(Long.class.getName(), ""+Long.MIN_VALUE+"L");
        
        defaultValueMap.put(double.class.getName(), ""+Double.MIN_VALUE+"D");
        defaultValueMap.put(Double.class.getName(), ""+Double.MIN_VALUE+"D");
        
        defaultValueMap.put(boolean.class.getName(), ""+false);
        defaultValueMap.put(Boolean.class.getName(), ""+false);
        
        defaultValueMap.put(byte.class.getName(), ""+Byte.MIN_VALUE);
        defaultValueMap.put(Byte.class.getName(), ""+Byte.MIN_VALUE);
        
        defaultValueMap.put(short.class.getName(), ""+Short.MIN_VALUE);
        defaultValueMap.put(Short.class.getName(), ""+Short.MIN_VALUE);
        
        defaultValueMap.put(char.class.getName(), ""+Character.MIN_VALUE);
        defaultValueMap.put(Character.class.getName(), ""+Character.MIN_VALUE);
    }
    
    public static String getDefaultValueString(Field f){
        
        Class<?> c = f.getType();
        
        if(defaultValueMap.containsKey(c.getName())){
            return defaultValueMap.get(c.getName());
        }
        
        if(c.isArray()){
            return "{}";
        }
        
        if(c.isEnum()){
            return c.getCanonicalName()+ "." + c.getEnumConstants()[0];
        }
        
        if((f.getType() == Class.class) && f.getGenericType().getTypeName().contains(" extends ")){
            String[] parts = 
                    f.getGenericType().getTypeName().split(" extends ");
            
            String className = parts[1].replace(">", "");
            
            className = className.replace("$", ".");
            
            return className + ".class";
        }
        
        if((f.getType() == Class.class)){
            return c.getCanonicalName() + ".class";
        }
        
//        if(c == Class.class){
//            return Class.class.getName();
//        }
        
        return null;
    }
    
    private static void main(String[] args){
        
    }
    
    private static class Test{
        private Class<? extends Test> aField;
    }
}
