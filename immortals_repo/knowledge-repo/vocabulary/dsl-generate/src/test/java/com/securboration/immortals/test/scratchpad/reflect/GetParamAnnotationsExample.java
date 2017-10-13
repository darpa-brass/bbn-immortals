package com.securboration.immortals.test.scratchpad.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.Image;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.Location;

public class GetParamAnnotationsExample {
    
    public static void main(String[] args) throws NoSuchMethodException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException{
        
        final Class<?> c = GetParamAnnotationsExample.class;
        
        for(Method m:c.getDeclaredMethods()){
            int index = 0;
            
            System.out.printf("examining method %s\n", m.getName());
            for(Annotation[] paramAnnotations:m.getParameterAnnotations()){
                System.out.printf(
                    "\tparameter %s (java type = %s) " +
                    "has the following %d annots\n", 
                    m.getParameters()[index].getName(), 
                    m.getParameterTypes()[index].getName(),
                    paramAnnotations.length
                    );
                
                for(Annotation a:paramAnnotations){
                    Class<?> annotationType = a.getClass();
                    String uri = (String)annotationType.getField(
                        "SEMANTIC_URI"
                        ).get(null);
                    
                    System.out.printf(
                        "\t\t%s : %s\n", 
                        a.annotationType().getSimpleName(),
                        uri);
                }
                
                index++;
            }
        }
        
        
    }
    
    @SuppressWarnings("unused")
    private void setLocation(
            @Location
            String s,
            @Image
            byte[] jpeg
            ){
        
    }

}
