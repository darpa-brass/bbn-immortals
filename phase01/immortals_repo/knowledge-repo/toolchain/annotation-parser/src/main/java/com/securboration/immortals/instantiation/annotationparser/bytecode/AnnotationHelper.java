package com.securboration.immortals.instantiation.annotationparser.bytecode;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

public class AnnotationHelper {
    
    public static List<AnnotationNode> getAnnotations(ClassNode cn){
        
        List<AnnotationNode> annotations = new ArrayList<>();
        
        if(cn.visibleAnnotations != null){
            for(AnnotationNode a:cn.visibleAnnotations){
                annotations.add(a);
            }
        }
        
        return annotations;
        
    }
    
    public static List<AnnotationNode> getAnnotations(MethodNode mn){
        
        List<AnnotationNode> annotations = new ArrayList<>();
        
        if(mn.visibleAnnotations != null){
            for(AnnotationNode a:mn.visibleAnnotations){
                annotations.add(a);
            }
        }
        
        return annotations;
        
    }
    
    public static Map<MethodNode,List<AnnotationNode>> gatherMethodAnnotations(
            ClassNode cn
            ){
        Map<MethodNode,List<AnnotationNode>> map = new HashMap<>();
        
        if(cn.methods == null){
            return map;
        }
        
        for(MethodNode mn:cn.methods){
            map.put(mn, getAnnotations(mn));
        }
        
        return map;
    }
    
    public static Class<?> getClass(final String desc){
        try{
            return Class.forName(Type.getType(desc).getClassName());
        } catch(ClassNotFoundException e) {
            return null;
        }
    }
    
    public static Class<?> getAnnotationClass(AnnotationNode a){
        try{
            return Class.forName(Type.getType(a.desc).getClassName());
        } catch(ClassNotFoundException e) {
            return null;
        }
    }
    
    private static String getSemanticUri(Class<?> c){
        try {
            Field f = c.getField("SEMANTIC_URI");
            
            return (String)f.get(null);
        } catch (NoSuchFieldException|SecurityException|IllegalAccessException e) {
            return null;
        }
    }
    
    public static String getParameterName(
            MethodNode mn,
            int index
            ){
        String name = null;
        
        //try to get it from parameters
        {
            ParameterNode parameter = 
                    AnnotationHelper.getParameter(mn, index);
            if(parameter != null){
                name = parameter.name;
            }
        }
        
        //try to get it from locals
        if(name != null){
            if(mn.localVariables != null){
                
                final boolean isStatic = (mn.access & Opcodes.ACC_STATIC) > 0;
                int offset = isStatic ? 0 : 1;
                
                name = mn.localVariables.get(offset + index).name;
            }
        }
        
        if(name != null){
            return name;
        }
        
        return "unknown [no name persisted in bytecode]";
    }
    
    public static AnnotationNode getOneOrNull(Collection<AnnotationNode> a){
        if(a.size() == 0){
            return null;
        }
        
        if(a.size() > 1){
            throw new RuntimeException(
                "expected exactly 1 or 0 but found " + a.size());
        }
        
        return a.iterator().next();
    }
    
    public static ParameterNode getParameter(
            MethodNode mn,
            int index
            ){
        if(mn.parameters == null){
            return null;
        }
        
        return mn.parameters.get(index);
    }
    
    public static List<AnnotationNode> getAnnotationsForParameter(
            MethodNode mn, 
            int index
            ){
        List<AnnotationNode> annotations = new ArrayList<>();
        
        if(mn.visibleParameterAnnotations == null){
            return annotations;
        }
        
        List<AnnotationNode> visibleAnnotations = 
                mn.visibleParameterAnnotations[index];
        
        if(visibleAnnotations != null){
            annotations.addAll(visibleAnnotations);
        }
        
        return annotations;
    }
    
    public static Class<?> getBackingPojo(Class<?> c){
        try {
            Field f = c.getField("BACKING_POJO");
            
            return (Class<?>)f.get(null);
        } catch (NoSuchFieldException|SecurityException|IllegalAccessException e) {
            return null;
        }
    }
    
    
    public static boolean isImmortalsAnnotation(Class<?> c){
        
        if(getSemanticUri(c) == null){
            return false;
        }
        
        if(getBackingPojo(c) == null){
            return false;
        }
        
//        mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation.BACKING_POJO;
        
        return true;
    }
    
    public static List<AnnotationNode> getAnnotationsDerivedFromType(
            Collection<AnnotationNode> annotations, 
            Class<?> baseType
            ){
        
        List<AnnotationNode> matches = new ArrayList<>();
        
        for(AnnotationNode a:annotations){
            Class<?> c = getAnnotationClass(a);
            
            if(c == null){
                continue;
            }
            
            if(!isImmortalsAnnotation(c)){
                continue;
            }
            
            Class<?> backingPojo = getBackingPojo(c);
            
            if(baseType.isAssignableFrom(backingPojo)){
                matches.add(a);
            }
        }
        
        return matches;
        
    }
    
    public static List<AnnotationNode> getAnnotationsOfType(
            Collection<AnnotationNode> annotations,
            Class<?> annotationClass
            ){
        
        final String targetDesc = 
                Type.getType(annotationClass).getDescriptor();
        
        List<AnnotationNode> matches = new ArrayList<>();
        
        for(AnnotationNode a:annotations){
            if(a.desc.equals(targetDesc)){
                matches.add(a);
            }
        }
        
        return matches;
    }
    
    public static Map<String,Object> getAnnotationKeyValues(
            AnnotationNode a
            ){
        List<String> pathSoFar = new ArrayList<>();
        pathSoFar.add("@"+Type.getType(a.desc).getInternalName());
        
        Map<String,Object> kvs = new HashMap<>();
        
        if(a.values == null){
            return kvs;
        }
        
        for(int i=0;i<a.values.size();i+=2){
            String key = (String)a.values.get(i);
            Object value = a.values.get(i+1);
            
            List<String> newPath = new ArrayList<>(pathSoFar);
            newPath.add(key);
            
            getAnnotationKeyValues(kvs,newPath,key,value);
        }
        
        return kvs;
    }
    
    private static String getPathString(List<String> path){
        
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<path.size();i++){
            
            if(i > 0){
                sb.append("|");
            }
            sb.append(path.get(i));
        }
        
        return sb.toString();
    }
    
    private static void getAnnotationKeyValues(
            Map<String,Object> kvs,
            List<String> pathSoFar,
            String key,
            Object value
            ){
        
        if(value instanceof AnnotationNode){
            //it's an annotation on an annotation
            
            AnnotationNode a = (AnnotationNode)value;
            
            List<Object> values = new ArrayList<>();
            if(a.values != null){
                values = a.values;
            }
            
            for(int i=0;i<values.size()/2;i+=2){
                String k = (String)values.get(i);
                Object v = values.get(i+1);
                
                List<String> newPath = new ArrayList<>(pathSoFar);
                newPath.add(key);
                newPath.add("@"+a.desc);
                newPath.add(k);
                
                //recurse
                getAnnotationKeyValues(kvs,newPath,k,v);
            }
            
            return;
        } else if(value.getClass().isArray()){
            
            //If it's an array, we have two possible scenarios:
            //1) it's an array of String with length 2 (for enum types)
            //2) it's a primitive array
            
            if(value.getClass().getName().contains("[L")){
                //if it's non-primitive, it must be a String array
                if(!Type.getType(value.getClass()).getElementType().equals(Type.getType(String.class))){
                    throw new RuntimeException("expected an array of String but found " + value.getClass().getName());
                }
                
                if(Array.getLength(value) != 2){
                    throw new RuntimeException(
                            "expected length 2 but found " + 
                                    Array.getLength(value));
                }
                
                //if it's a String array, the value part is an enumeration value
                // v[0] is the descriptor of the enum
                // v[1] is the string value of the enum selected
                kvs.put(
                        getPathString(pathSoFar),
                        ((Object[])value)[1].toString()
                        );
                
                return;
            } else {
                //otherwise, the value is a primitive array (e.g., array of ints)
                for(int i=0;i<Array.getLength(value);i++){
                    Object o = Array.get(value,i);
                    
                    List<String> newPath = new ArrayList<>(pathSoFar);
                    newPath.add("["+i+"]");
                    
                    //recurse
                    getAnnotationKeyValues(kvs,newPath,key,o);
                    
                    i++;
                }
                return;
            }
        } else if(value instanceof List){
            //the value is an array of other values
            int i = 0;
            for(Object o:(List<?>)value){
                
                List<String> newPath = new ArrayList<>(pathSoFar);
                newPath.add("["+i+"]");
                
                //recurse
                getAnnotationKeyValues(kvs,newPath,key,o);
                
                i++;
            }
            return;
        } else if(value instanceof Type){
            //the value is a Class<?> type
            
            kvs.put(
                    getPathString(pathSoFar),
                    "TYPE("+((Type)value).getClassName()+")"
                    );
            
            return;
        } else {
            //the value is a primitive
            
            kvs.put(
                    getPathString(pathSoFar), 
                    value.toString()
                    );
        
            return;
        }
    }

}
