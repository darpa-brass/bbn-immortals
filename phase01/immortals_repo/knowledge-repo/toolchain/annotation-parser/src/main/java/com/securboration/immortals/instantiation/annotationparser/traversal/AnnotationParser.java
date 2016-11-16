package com.securboration.immortals.instantiation.annotationparser.traversal;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.securboration.immortals.instantiation.annotationparser.bytecode.AnnotationHelper;
import com.securboration.immortals.instantiation.annotationparser.bytecode.ArrayHelper;
//import com.securboration.immortals.instantiation.annotationparser.bytecode.ArrayHelper;
import com.securboration.immortals.instantiation.annotationparser.bytecode.BytecodeHelper;
import com.securboration.immortals.instantiation.annotationparser.bytecode.Console;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.dfu.instance.ArgToSemanticTypeBinding;
import com.securboration.immortals.ontology.dfu.instance.DfuInstance;
import com.securboration.immortals.ontology.dfu.instance.FunctionalAspectInstance;
import com.securboration.immortals.ontology.dfu.instance.ReturnValueToSemanticTypeBinding;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.property.Property;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.instance.Recipe;

public class AnnotationParser implements BytecodeArtifactVisitor {
    
    private final ObjectToTriplesConfiguration config;
    
    public AnnotationParser(
            ObjectToTriplesConfiguration config
            ){
        this.config = config;
    }
    
    private Object instantiateFromAnnotation(
            AnnotationNode annotation,
            Class<?> annotationType,
            Class<?> typeToInstantiate
            ) throws InstantiationException, IllegalAccessException{
        
        Object o = typeToInstantiate.newInstance();
        
        annotation.accept(new ObjectBuilderVisitor.Visitor(o));
        
        return o;
    }
    
    private Property[] getProperties(Collection<AnnotationNode> annotations){
        List<Property> properties = new ArrayList<>();
        
        List<AnnotationNode> propertyAnnotations = 
                AnnotationHelper.getAnnotationsDerivedFromType(
                    annotations, 
                    Property.class
                    );
        
        for(AnnotationNode p:propertyAnnotations){
            Class<?> annotationClass = 
                    AnnotationHelper.getAnnotationClass(p);
            
            Class<?> backingClass = 
                    AnnotationHelper.getBackingPojo(annotationClass);
            
            try {
                Property property = 
                        (Property) instantiateFromAnnotation(
                            p,
                            annotationClass,
                            backingClass
                            );
                
                properties.add(property);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        
        return properties.toArray(new Property[]{});
    }

    @Override
    public void visitClass(
            String classHash, 
            byte[] bytecode
            ) {
        
        ClassNode cn = BytecodeHelper.getClassNode(bytecode);
        
        Collection<AnnotationNode> classAnnotations = 
                AnnotationHelper.getAnnotations(cn);
        
        List<AnnotationNode> dfuAnnotations = 
                AnnotationHelper.getAnnotationsOfType(
                    classAnnotations, 
                    DfuAnnotation.class
                    );
        
        AnnotationNode dfuAnnotation = 
                AnnotationHelper.getOneOrNull(dfuAnnotations);
        
        if(dfuAnnotation == null){
            return;
        }
        
        Console.log("\nfound a DFU: " + cn.name);
        
        Map<String,Object> kvs = 
                AnnotationHelper.getAnnotationKeyValues(dfuAnnotation);
        
        for(String key:new TreeSet<>(kvs.keySet())){
            Console.log("\t\t%s <-- %s (v is a %s)",kvs.get(key),key,kvs.get(key).getClass().getName());
        }
        
        Map<MethodNode, Class<? extends FunctionalAspect>> aspectMap = 
                getFunctionalAspectMap(
                    AnnotationHelper.gatherMethodAnnotations(cn)
                    );
        
        {
            DfuInstance dfu = new DfuInstance();
            
            final Class<? extends Functionality> abstractFunctionality = 
                    getFunctionality(kvs);
            
            {
                if(!OntologyHelper.isConceptInstance(abstractFunctionality)){
                    throw new RuntimeException(abstractFunctionality + " is not a concept instance!");
                }
            }
            
            final Functionality abstractFunctionalityInstance = 
                    instantiateFunctionality(abstractFunctionality);
            
            dfu.setFunctionalityAbstraction(abstractFunctionality);
            
            dfu.setClassPointer(classHash);
            
            dfu.setResourceDependencies(
                ArrayHelper.append(
                    Class.class, 
                    getResourcesFromDfu(kvs),
                    new Class[]{}//abstractFunctionalityInstance.getResourceDependencies()
                    )
                );
            
            dfu.setFunctionalAspects(
                getAspectInstances(
                    aspectMap,
                    classHash
                    )
                );
            
            dfu.setDfuProperties(
                ArrayHelper.append(
                    Property.class, 
                    getProperties(
                        AnnotationHelper.getAnnotationsDerivedFromType(
                            classAnnotations,
                            Property.class
                            )
                        ),
                    new Property[]{}//abstractFunctionalityInstance.getFunctionalityProperties()
                    )
                );
            
            dfu.setTag(
                (String)kvs.get(Keys.tag));
            
            config.getMapper().registerObjectToSerialize(dfu);
        }
    }
    
    private Functionality instantiateFunctionality(Class<? extends Functionality> c){
        try {
            return c.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    private FunctionalAspect instantiate(Class<? extends FunctionalAspect> c){
        try {
            return c.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static AnnotationNode getRecipeAnnotation(MethodNode mn){
        
        List<AnnotationNode> recipeAnnotations = 
                AnnotationHelper.getAnnotationsOfType(
                    mn.visibleAnnotations, 
                    Recipe.class
                    );
        
        if(recipeAnnotations.size() == 0){
            return null;
        }
        
        if(recipeAnnotations.size() > 1){
            throw new RuntimeException(
                "found multiple Recipe annotations on " + mn.name + 
                " but expected at most 1"
                );
        }
        
        return recipeAnnotations.get(0);
    }
    
    private static AnnotationNode getFunctionalAspectAnnotation(MethodNode mn){
        
        List<AnnotationNode> methodAnnotations = 
                AnnotationHelper.getAnnotations(mn);
        
        List<AnnotationNode> functionalAspectAnnotations = 
                AnnotationHelper.getAnnotationsOfType(
                    methodAnnotations,
                    FunctionalAspectAnnotation.class
                    );
        
        if(functionalAspectAnnotations.size() > 1){
            throw new RuntimeException(
                "the parser expected at most 1 functional aspect annotation on method " + mn.name);
        }
        
        if(functionalAspectAnnotations.size() == 0){
            return null;
        }
        
        return functionalAspectAnnotations.get(0);
    }
    
    private Class<? extends DataType> getDataType(
            List<AnnotationNode> annotations
            ){
        List<AnnotationNode> typeAnnotations = 
                AnnotationHelper.getAnnotationsDerivedFromType(
                    annotations, 
                    DataType.class
                    );
        AnnotationNode typeAnnotation = 
                AnnotationHelper.getOneOrNull(typeAnnotations);
        
        if(typeAnnotation == null){
            return null;
        }
        
        Class<?> annotationClass = getClassFromAnnotationType(
                    Type.getType(typeAnnotation.desc).getClassName());
        
        return (Class<? extends DataType>) 
                AnnotationHelper.getBackingPojo(annotationClass);
    }
    
    private ReturnValueToSemanticTypeBinding getReturnValue(
            MethodNode mn,
            FunctionalAspect aspectImplemented
            ){
        
        Type method = Type.getMethodType(mn.desc);
        final Type[] argTypes = method.getArgumentTypes();
        final Type returnType = method.getReturnType();
        
        List<AnnotationNode> argAnnotations = 
                AnnotationHelper.getAnnotations(mn);
        
        //TODO: for now there is no way to distinguish between aspect 
        //       properties and properties applicable to the return value
        
        Class<? extends DataType> explicitDataType = 
                getDataType(argAnnotations);
        
        ReturnValueToSemanticTypeBinding b = 
                new ReturnValueToSemanticTypeBinding();
        b.setArgIndex(-1);
        b.setProperties(getProperties(argAnnotations));
        b.setSemanticType(explicitDataType);
        
        StringBuilder sb = new StringBuilder();
        
        sb.append(
            String.format(
                "return value is a %s with explicit semantic type %s",
                returnType.getDescriptor(),
                explicitDataType != null ? explicitDataType.getSimpleName() : "[not provided]"
                )
            );
        
        b.setComment(sb.toString());
        
        return b;
    }
    
    private ArgToSemanticTypeBinding[] getArgs(
            MethodNode mn,
            FunctionalAspect aspectImplemented
            ){
        
        Type method = Type.getMethodType(mn.desc);
        final Type[] argTypes = method.getArgumentTypes();
        
        List<ArgToSemanticTypeBinding> argMappings = new ArrayList<>();
        
        int argIndex = 0;
        for(Type t:argTypes){
            List<AnnotationNode> argAnnotations = 
                    AnnotationHelper.getAnnotationsForParameter(mn, argIndex);
            
            Class<? extends DataType> explicitDataType = 
                    getDataType(argAnnotations);
            
            final String parameterName = 
                    AnnotationHelper.getParameterName(mn, argIndex);
            
            ArgToSemanticTypeBinding b = new ArgToSemanticTypeBinding();
            b.setArgIndex(argIndex);
            b.setProperties(getProperties(argAnnotations));
            b.setSemanticType(explicitDataType);
            
            StringBuilder sb = new StringBuilder();
            
            sb.append(
                String.format(
                    "arg%d (%s) is a %s with explicit semantic type %s", 
                    argIndex,
                    parameterName,
                    t.getDescriptor(),
                    explicitDataType != null ? explicitDataType.getSimpleName() : "[not provided]"
                    )
                );
            
            b.setComment(sb.toString());
            
            argMappings.add(b);
            
            argIndex++;
        }
        
        
        return argMappings.toArray(new ArgToSemanticTypeBinding[]{});
    }
    
    private FunctionalAspectInstance[] getAspectInstances(
            Map<MethodNode, Class<? extends FunctionalAspect>> methodsToAspects,
            final String classHash
            ){
        
        List<FunctionalAspectInstance> aspects = new ArrayList<>();
        
        for(MethodNode mn:methodsToAspects.keySet()){
            AnnotationNode aspectAnnotation = 
                    getFunctionalAspectAnnotation(mn);
            
            Map<String,Object> kvs = 
                    AnnotationHelper.getAnnotationKeyValues(aspectAnnotation);
            
            FunctionalAspectInstance aspect = new FunctionalAspectInstance();
            
            {//add the recipe value
                AnnotationNode a = getRecipeAnnotation(mn);
                
                if(a != null){
                    Map<String,Object> map = 
                            AnnotationHelper.getAnnotationKeyValues(a);
                    
                    String recipe = (String)map.get(Keys.recipe);
                    
                    aspect.setRecipe(recipe);
                }
            }
            
            final Class<? extends FunctionalAspect> abstractAspect = 
                    methodsToAspects.get(mn);
            FunctionalAspect abstractAspectInstance = 
                    instantiate(abstractAspect);
            
            final String pointer = classHash + "/methods/" + mn.name + mn.desc;
            aspect.setMethodPointer(pointer);
            
            aspect.setAbstractAspect(abstractAspect);
            aspect.setArgsToSemanticTypes(getArgs(mn,abstractAspectInstance));
            aspect.setReturnValueToSemanticType(getReturnValue(mn,abstractAspectInstance));
            
            List<AnnotationNode> methodAnnotations = 
                    AnnotationHelper.getAnnotations(mn);
            
            List<AnnotationNode> propertyAnnotations = 
                    AnnotationHelper.getAnnotationsDerivedFromType(
                        methodAnnotations, 
                        Property.class
                        );
            
            aspect.setProperties(
                ArrayHelper.append(
                    Property.class, 
                    getProperties(propertyAnnotations),//declared properties
                    new Property[]{}//abstractAspectInstance.getAspectProperties()//abstract model's properties
                    )
                );
            
            aspect.setResourceDependencies(
                ArrayHelper.append(
                    Class.class, 
                    getResourcesFromAspect(kvs),
                    new Class[]{}//abstractAspectInstance.getAspectSpecificResourceDependencies()
                    )
                );
            
            aspects.add(aspect);
        }
        
        return aspects.toArray(new FunctionalAspectInstance[]{});
    }
    
    private static Map<MethodNode,Class<? extends FunctionalAspect>> getFunctionalAspectMap(
            Map<MethodNode,List<AnnotationNode>> annotatedMethods
            ){
        Map<MethodNode,Class<? extends FunctionalAspect>> map = 
                new HashMap<>();
        
        for(MethodNode mn:annotatedMethods.keySet()){
            AnnotationNode a = getFunctionalAspectAnnotation(mn);
            
            if(a == null){
                continue;
            }
            
            Console.log("in method %s, found the following functional aspect annotation:", mn.name);
            
            Map<String,Object> kvs = 
                    AnnotationHelper.getAnnotationKeyValues(a);
            
            final String functionalAspectType = 
                    (String)kvs.get(Keys.functionalAspect);
            
            Class<? extends FunctionalAspect> c = 
                    (Class<? extends FunctionalAspect>) getClassFromAnnotationType(functionalAspectType);
            
            if(!OntologyHelper.isConceptInstance(c)){
                throw new RuntimeException(c.getName() + " is not a concept instance!");
            }
            
            map.put(mn, c);
        }
        
        return map;
    }
    
    private static class Keys{
        
        private static final String functionalityBeingPerformed = 
                "@mil/darpa/immortals/annotation/dsl/ontology/dfu/annotation/DfuAnnotation|functionalityBeingPerformed";
        
        private static final String resourceDependenciesDfu = 
                "@mil/darpa/immortals/annotation/dsl/ontology/dfu/annotation/DfuAnnotation|resourceDependencies|";
        
        private static final String resourceDependenciesAspect = 
                "@mil/darpa/immortals/annotation/dsl/ontology/dfu/annotation/FunctionalAspectAnnotation|aspectSpecificResourceDependencies|";
        
        private static final String functionalAspect = 
                "@mil/darpa/immortals/annotation/dsl/ontology/dfu/annotation/FunctionalAspectAnnotation|aspect";
        
        private static final String tag = 
                "@mil/darpa/immortals/annotation/dsl/ontology/dfu/annotation/DfuAnnotation|tag";
        
        private static final String recipe = 
                "@mil/darpa/immortals/annotation/dsl/ontology/dfu/instance/Recipe|recipe";
    }
    
    private static Class<?> getClassFromAnnotationType(final String value){
        
        final String className = 
                value.replace("/", ".").replace("TYPE(", "").replace(")","");
        
        try {
            Class<?> c = Class.forName(className);
            
            return c;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    private static Class<? extends Resource>[] getResourcesFromAspect(
            Map<String,Object> kvs
            ){
        List<String> resourceClassNames = 
                getArrayValues(
                    kvs,
                    Keys.resourceDependenciesAspect
                    );
        
        Class<?>[] classes = new Class[resourceClassNames.size()];
        int i=0;
        for(String resourceClassName:resourceClassNames){
            classes[i] = getClassFromAnnotationType(resourceClassName);
            i++;
        }
        
        return (Class<? extends Resource>[]) classes;
    }
    
    private static Class<? extends Resource>[] getResourcesFromDfu(
            Map<String,Object> kvs
            ){
        List<String> resourceClassNames = 
                getArrayValues(
                    kvs,
                    Keys.resourceDependenciesDfu
                    );
        
        Class<?>[] classes = new Class[resourceClassNames.size()];
        int i=0;
        for(String resourceClassName:resourceClassNames){
            classes[i] = getClassFromAnnotationType(resourceClassName);
            i++;
        }
        
        return (Class<? extends Resource>[]) classes;
    }
    
    private static Class<? extends Functionality> getFunctionality(
            Map<String,Object> kvs
            ){
        final String value = (String)kvs.get(Keys.functionalityBeingPerformed);
        
        return (Class<? extends Functionality>) getClassFromAnnotationType(value);
    }
    
    private static <T> List<T> getArrayValues(Map<String,Object> map, String key){
        List<T> matches = new ArrayList<>();
        
        int counter = 0;
        boolean stop = false;
        while(!stop){
            stop = true;
            
            String currentTarget = key + "[" + counter + "]";
            
            if(map.containsKey(currentTarget)){
                matches.add((T)map.get(currentTarget));
                stop = false;
            }
            
            counter++;
        }
        
        return matches;
    }
    
    private static void addPathComponent(
            StringBuilder sb,
            Class<? extends Annotation> a, 
            String fieldName
            ){
        
        sb.append("@");
        sb.append(Type.getType(a).getDescriptor());
        sb.append("|");
        sb.append(fieldName);
    }
    
    private <T> T template(
            Class<T> t,
            String uri
            ){
        
        try{
            T templateObject = t.newInstance();
            
            config.getMapper().registerMapping(templateObject, uri);
            
            return templateObject;
        } catch(InstantiationException|IllegalAccessException e){
            throw new RuntimeException(e);
        }
    }
    
    
    
    

}
