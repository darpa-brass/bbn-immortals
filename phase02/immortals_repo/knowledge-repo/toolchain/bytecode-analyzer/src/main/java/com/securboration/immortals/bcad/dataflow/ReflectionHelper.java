package com.securboration.immortals.bcad.dataflow;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Type;

public class ReflectionHelper {
    
    public static Method findMethod(
            Class<?> c,
            final String methodName, 
            final String methodDesc
            ){
        
        List<Class<?>> path = new ArrayList<>();
        
        boolean stop = false;
        
        while(!stop){
            
            path.add(c);
            
            for(Method m:c.getDeclaredMethods()){
                
                final boolean nameMatches = m.getName().equals(methodName);
                final boolean descMatches = Type.getMethodDescriptor(m).equals(methodDesc);
                
                if(nameMatches && descMatches){
                    return m;
                }
            }
            
            c = c.getSuperclass();
            
            if(c == null){
                stop = true;
            }
        }
        
        throw new RuntimeException(
            "unable to find " + methodName + " " + methodDesc + " along path " + path
            );
    }

}
