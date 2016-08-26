package com.securboration.immortals.annotations.helper;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class AnnotationHelper {

    public static <T extends Annotation> T getAnnotationOfType(
            Class<?> annotatedClass,
            Class<T> annotationType
            ){
        return annotatedClass.getAnnotation(annotationType);
    }
    
    public static List<Annotation> getAnnotationsOfType(
            Class<?> annotatedClass,
            Class<?> annotationType
            ){
        List<Annotation> annotations = new ArrayList<>();
        
        for(Annotation a:annotatedClass.getAnnotations()){
            if(equals(a.annotationType(),annotationType)){
                annotations.add(a);
            }
        }
        
        return annotations;
    }
    
    public static boolean containsAnnotation(
            Class<?> annotatedClass,
            Class<?> annotationType
            ){
        return getAnnotationsOfType(annotatedClass,annotationType).size() > 0;
    }
    
    private static boolean equals(Class<?> c1, Class<?> c2){
        if(c1.getName().equals(c2.getName())){
            return true;
        }
        
        return false;
    }
    
}
