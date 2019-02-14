package com.securboration.immortals.annotations.helper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.annotations.triples.Triples;

public class AnnotationHelper {
    
    public static Triple[] getTriples(Class<?> c){
        
        List<Triple> triples = new ArrayList<>();
        
        for(Triple t:c.getAnnotationsByType(Triple.class)){
            triples.add(t);
        }
        
        for(Triples ts:c.getAnnotationsByType(Triples.class)){
            for(Triple t:ts.value()){
                triples.add(t);
            }
        }
        
        return triples.toArray(new Triple[]{});
    }
    
    public static Triple[] getTriples(Method m){
        
        List<Triple> triples = new ArrayList<>();
        
        for(Triple t:m.getAnnotationsByType(Triple.class)){
            triples.add(t);
        }
        
        for(Triples ts:m.getAnnotationsByType(Triples.class)){
            for(Triple t:ts.value()){
                triples.add(t);
            }
        }
        
        return triples.toArray(new Triple[]{});
    }
    
    public static Triple[] getTriples(Field f){
        
        List<Triple> triples = new ArrayList<>();
        
        for(Triple t:f.getAnnotationsByType(Triple.class)){
            triples.add(t);
        }
        
        for(Triples ts:f.getAnnotationsByType(Triples.class)){
            for(Triple t:ts.value()){
                triples.add(t);
            }
        }
        
        return triples.toArray(new Triple[]{});
    }

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
