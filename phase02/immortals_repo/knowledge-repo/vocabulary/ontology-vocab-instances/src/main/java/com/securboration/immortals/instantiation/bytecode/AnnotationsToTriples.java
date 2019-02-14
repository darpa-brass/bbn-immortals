package com.securboration.immortals.instantiation.bytecode;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LocalVariableAnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

import com.securboration.immortals.ontology.bytecode.AClass;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.dfu.Dfu;
import com.securboration.immortals.ontology.dfu.Dfus;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;

/*
 Deprecated; use JarTraversal to translate annotations
 */
public class AnnotationsToTriples {
    
    public AnnotationsToTriples(UriMappings uriMappings){
        this.uriMappings = uriMappings;
    }
    
    private final UriMappings uriMappings;
    private List<Dfu> dfus = new ArrayList<>();
    
    private final List<ClassNode> classes = new ArrayList<>();
    
    public Dfus getDfus(){
        Dfus dfus = new Dfus();
        
        dfus.setDfus(this.dfus.toArray(new Dfu[]{}));
        
        return dfus;
    }
    
    private Map<MethodNode,MethodAnnotations> getMethodAnnotations(ClassNode cn){
        
        Map<MethodNode,MethodAnnotations> map = new HashMap<>();
        for(MethodNode mn:cn.methods){
            map.put(mn, new MethodAnnotations(mn));
        }
        
        return map;
    }
    
    public void visitClass(
            AClass classPojo,
            ClassNode cn
            ){
        
        classes.add(cn);
        
        if(true){
            return;//TODO
        }
        
        List<AnnotationNode> classAnnotations = 
                getClassAnnotations(cn);
        
        Collection<AnnotationNode> dfuAnnotations = 
                getAnnotationsOfType(
                        UriMappings.AnnotationDescriptors.Dfu,
                        classAnnotations);
        
        if(dfuAnnotations.size() == 0){
            //do nothing
        } else if(dfuAnnotations.size() == 1){
            //instantiate the dfu
            AnnotationNode dfuAnnotation = dfuAnnotations.iterator().next();
            
            Dfu dfu = instantiateDfu(classPojo,cn,dfuAnnotation);
            
            dfus.add(dfu);
        } else {
            throw new RuntimeException(
                    "expected 0 or 1 @Dfu annotations on class " + 
                            cn.name + " but found " + dfuAnnotations.size());
        }
    }
    
    
    
    /**
     * 
     * @param mn
     * @param index if -1, returns the semantic type of the method's returned 
     * value.  Otherwise, returns the semantic type of the method argument at the indicated index
     * @return the semantic type of the argument at the indicated index
     */
    private static String getSemanticType(
            MethodNode mn,
            int index
            ){
        List<AnnotationNode> annotationsToSearch = new ArrayList<>();
        
        if(index == -1){
            
            addAll(mn.visibleAnnotations,annotationsToSearch);
        } else {
            addAll(mn.visibleParameterAnnotations[index],annotationsToSearch);
        }
        
        Collection<AnnotationNode> typeAnnotations = 
                getAnnotationsOfType(
                        UriMappings.AnnotationDescriptors.SemanticTypeBinding,
                        annotationsToSearch);
        
        if(typeAnnotations.size() == 0){
            return null;
        }
        
        if(typeAnnotations.size() > 1){
            throw new RuntimeException("expected exactly one @SemanticTypeBinding on " + mn.name + " index=" + index + " but found " + typeAnnotations.size());
        }
        
        AnnotationNode typeAnnotation = typeAnnotations.iterator().next();
        
        Map<String,Object> annotationValues = 
                getAnnotationKeyValues(typeAnnotation);
        
        return (String)annotationValues.get(
                getAnnotationPath(
                        UriMappings.AnnotationDescriptors.SemanticTypeBinding,
                        "semanticType")
                );
    }
    
    private static String getAnnotationPath(
            String annotationDesc,
            String...components
            ){
        String annotationName = Type.getType(annotationDesc).getInternalName();
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("@");
        sb.append(Type.getType(annotationName).getInternalName());
        
        for(String component:components){
            sb.append("|");
            
            if(component.contains("/")){
                sb.append("@");
            }
            
            sb.append(component);
        }
        
        return sb.toString();
    }
    
    private Output getOutput(MethodNode mn){
        final String uri = getSemanticType(mn,-1);
        
        if(uri == null){
            return null;
        }
        
        Output output = new Output();
        output.setType(uriMappings.mapSemanticUriToDatatype(uri));
        
        return output;
    }
    
    private Input[] getInputs(MethodNode mn){
        
        List<Input> inputs = new ArrayList<>();
        
        if(mn.parameters == null){
            return null;
        }
        
        for(int i=0;i<mn.parameters.size();i++){
            final String uri = getSemanticType(mn,i);
            
            if(uri != null){
                Input input = new Input();
                inputs.add(input);
                
                input.setType(uriMappings.mapSemanticUriToDatatype(uri));
            }
        }
        
        if(inputs.size() == 0){
            return null;
        }
        
        return inputs.toArray(new Input[]{});
    }
    
    private static Collection<MethodNode> getFunctionalAspects(
            ClassNode cn,
            String functionalityUri
            ){
        
        Collection<MethodNode> functionalAspects = new ArrayList<>();
        for(MethodNode mn:cn.methods){
            if(mn.visibleAnnotations == null){
                continue;
            }
            
            AnnotationNode dfuAnnotation = 
                    getAnnotationOfType(
                            UriMappings.AnnotationDescriptors.FunctionalDfuAspect,
                            mn.visibleAnnotations);
            
            if(dfuAnnotation == null){
                continue;
            }
            
            Map<String,Object> kvs = getAnnotationKeyValues(dfuAnnotation);
            
            String annotationFunctionalityUri = 
                    (String)kvs.get(
                            getAnnotationPath(
                                    UriMappings.AnnotationDescriptors.FunctionalDfuAspect,
                                    "functionalityUri"
                                    ));
            
            if(annotationFunctionalityUri == null){
                continue;
            }
            
            System.out.println("*** method " + mn.name + " is a functional aspect of " + functionalityUri);
            
            functionalAspects.add(mn);
        }
        
        return functionalAspects;

    }
    
    
    
    private static <T> Collection<T> getArrayValue(Map<String,Object> kvs,String key){
        List<T> values = new ArrayList<>();
        
        int index = 0;
        boolean stop = false;
        while(!stop){
            String indexedKey = key + "|[" + index + "]";
            
            if(kvs.containsKey(indexedKey)){
                values.add((T)kvs.get(indexedKey));//erasure issue
            } else {
                stop = true;
            }
            
            index++;
        }
        
        return values;
    }
    
    private static <T> Class<? extends T>[] getClasses(Collection<T> objects){
        Set<Class<? extends T>> classes = new HashSet<>();
        
        for(T o:objects){
            classes.add((Class<? extends T>) o.getClass());
        }
        
        return classes.toArray(new Class[]{});
    }
    
    private Dfu instantiateDfu(
            AClass aClass,
            ClassNode cn,
            AnnotationNode dfuAnnotationOnClass
            ){
        System.out.println("about to instantiate a dfu for class " + cn.name);
        System.out.println(getAnnotationKeyValues(dfuAnnotationOnClass));
        
        Dfu dfu = new Dfu();
        
        //DFU-level resource dependencies
        {
            Map<String,Object> kvs = 
                    getAnnotationKeyValues(dfuAnnotationOnClass);
            
            List<Resource> resourceDependencies = new ArrayList<>();
            
            Collection<String> dfuResourceDependencies = 
                    getArrayValue(
                            kvs,
                            getAnnotationPath(
                                    UriMappings.AnnotationDescriptors.Dfu,
                                    "resourceDependencies",
                                    "resourceDependencies",
                                    UriMappings.AnnotationDescriptors.ResourceDependencies,
                                    "dependencyUris"
                                    ));
            
            for(String resourceDependency:dfuResourceDependencies){
                resourceDependencies.add(
                        uriMappings.getInstance(
                                resourceDependency, 
                                Resource.class));
            }
            
            dfu.setResourceDependencies(
                    getClasses(
                            resourceDependencies));
        }
        
        //bytecode linkage
        {
            dfu.setCodeUnit(aClass);
        }
        
        //functionality
        {
            Map<String,Object> dfuAnnotationKvs = 
                    getAnnotationKeyValues(dfuAnnotationOnClass);
            
            final String functionalityUri = 
                    (String)dfuAnnotationKvs.get(
                            getAnnotationPath(
                                    UriMappings.AnnotationDescriptors.Dfu,
                                    "functionalityUri"
                                    ));
            
            if(functionalityUri == null){
                throw new RuntimeException("assumption violated");
            }
            
            Functionality functionality = 
                    uriMappings.getInstance(
                            functionalityUri,
                            Functionality.class);
            
            List<FunctionalAspect> functionalAspects = new ArrayList<>();
            
            //loop over all functional aspects of the dfu
            for(MethodNode mn:getFunctionalAspects(cn,functionalityUri)){
                
                AnnotationNode dfuAnnotation = 
                        getAnnotationOfType(
                                UriMappings.AnnotationDescriptors.FunctionalDfuAspect,
                                mn.visibleAnnotations);
                
                Map<String,Object> kvs = getAnnotationKeyValues(dfuAnnotation);
                
                String functionalAspectUri = 
                        (String)kvs.get(
                                getAnnotationPath(
                                        UriMappings.AnnotationDescriptors.FunctionalDfuAspect,
                                        "functionalAspectUri"
                                        ));
                
                if(functionalAspectUri == null){
                    throw new RuntimeException(
                            "no functional aspect annotation provided for " + 
                            cn.name + "." + mn.name + 
                            "(...) but it was annotated with @FunctionalDfuAspect"
                            );
                }
                
                FunctionalAspect aspect = 
                        uriMappings.getInstance(
                                functionalAspectUri,
                                FunctionalAspect.class);
                
                Output output = getOutput(mn);
                Input[] inputs = getInputs(mn);
                
                if(output == null && inputs == null){
                    continue;
                }
                
                //aspect-specific resource dependencies
                {
                    Collection<String> aspectResourceDependencies = 
                            getArrayValue(
                                    kvs,
                                    getAnnotationPath(
                                            UriMappings.AnnotationDescriptors.FunctionalDfuAspect,
                                            "resourceDependencies",
                                            "resourceDependencies",
                                            UriMappings.AnnotationDescriptors.ResourceDependencies,
                                            "dependencyUris"
                                            ));
                    
                    List<Resource> aspectDependencies = new ArrayList<>();
                    for(String aspectResourceDependency:aspectResourceDependencies){
                        aspectDependencies.add(
                                uriMappings.getInstance(
                                        aspectResourceDependency,
                                        Resource.class
                                        )
                                );
                    }
                    
                    aspect.setAspectSpecificResourceDependencies(
                            getClasses(
                                    aspectDependencies));
                }
                
                // TODO: these are only explicit inputs/outputs, it's possible for
                // additional implicit dataflows to be present
                aspect.setOutputs(new Output[]{output});
                aspect.setInputs(inputs);
                
                functionalAspects.add(aspect);
            }
            
            functionality.setFunctionalAspects(
                    functionalAspects.toArray(
                            new FunctionalAspect[]{}));
            
            dfu.setFunctionalityBeingPerformed(functionality);
        }
        
        return dfu;
    }
    
    private static Collection<AnnotationNode> getAnnotationsOfType(
            String annotationDesc,
            Collection<AnnotationNode> annotations
            ){
        
        List<AnnotationNode> matches = new ArrayList<>();
        
        for(AnnotationNode a:annotations){
            if(a.desc.equals(annotationDesc)){
                matches.add(a);
            }
        }
        
        return matches;
    }
    
    private static AnnotationNode getAnnotationOfType(
            String annotationDesc,
            Collection<AnnotationNode> annotations
            ){
        
        for(AnnotationNode a:annotations){
            if(a.desc.equals(annotationDesc)){
                return a;
            }
        }
        
        return null;
    }
    
    private static <T> void addAll(Collection<T> values, Collection<T> addTo){
        if(values == null){
            return;
        }
        
        addTo.addAll(values);
    }
    
    private static class MethodAnnotations{
        private final List<TypeAnnotationNode> typeAnnotations = new ArrayList<>();
        private final List<AnnotationNode> methodAnnotations = new ArrayList<>();
        private final List<LocalVariableAnnotationNode> localVariableAnnotations = new ArrayList<>();
        private final List<List<AnnotationNode>> parameterAnnotations = new ArrayList<>();
        
        public MethodAnnotations(MethodNode mn){
            addAll(mn.visibleTypeAnnotations,typeAnnotations);
            addAll(mn.visibleAnnotations,methodAnnotations);
            addAll(mn.visibleLocalVariableAnnotations,localVariableAnnotations);
            
            if(mn.visibleParameterAnnotations != null){
                for(List<AnnotationNode> visibleParameterAnnotations:mn.visibleParameterAnnotations){
                    List<AnnotationNode> annotationsForParameter = new ArrayList<>();
                    
                    addAll(visibleParameterAnnotations,annotationsForParameter);
                    
                    parameterAnnotations.add(annotationsForParameter);
                }
            }
        }
    }
    
    private static List<AnnotationNode> getClassAnnotations(ClassNode cn){
        if(cn.visibleAnnotations == null){
            return new ArrayList<>();
        }
        
        return new ArrayList<>(cn.visibleAnnotations);
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
    
    private static Map<FieldNode,List<AnnotationNode>> getFieldAnnotations(ClassNode cn){
        
        Map<FieldNode,List<AnnotationNode>> map = new HashMap<>();
        
        if(cn.fields == null){
            return map;
        }
        
        for(FieldNode fn:cn.fields){
            List<AnnotationNode> annotations = new ArrayList<>();
            map.put(fn,annotations);
            
            if(fn.visibleAnnotations != null){
                annotations.addAll(fn.visibleAnnotations);
            }
        }
        
        return map;
    }

    
    public List<ClassNode> getClasses() {
        return classes;
    }
}
