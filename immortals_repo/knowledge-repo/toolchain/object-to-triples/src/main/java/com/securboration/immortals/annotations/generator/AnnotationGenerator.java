package com.securboration.immortals.annotations.generator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.objectweb.asm.Type;

import com.securboration.immortals.annotations.helper.AnnotationHelper;
import com.securboration.immortals.j2t.analysis.ClasspathTraverser;
import com.securboration.immortals.j2t.analysis.ClasspathTraverser.ClasspathVisitor;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;

public class AnnotationGenerator {
    
    private final long timestamp = System.currentTimeMillis();
    
    private final AnnotationGeneratorConfiguration configuration;
    
    private final Map<Class<?>,Set<Class<?>>> parentsToChildren;
    private final Map<Class<?>,Set<Class<?>>> childrenToParents;
    
    private static final Set<Class<?>> primitives = 
            new HashSet<>(Arrays.asList(
                    Integer.class,
                    Float.class,
                    Long.class,
                    Double.class,
                    Byte.class,
                    Short.class,
                    Character.class,
                    Boolean.class,
                    
                    String.class,
                    Class.class
                    ));
    
    public static void generateAnnotations(
            AnnotationGeneratorConfiguration g
            ) throws ClassNotFoundException, IOException {
        
        ClassGathererVisitor classGatherer = 
                new ClassGathererVisitor();
        
        ClasspathTraverser t = 
                new ClasspathTraverser(g.getJavaToTriplesConfiguration());
        
        t.traverse(classGatherer);
        
        AnnotationGenerator generator = 
                new AnnotationGenerator(
                        g,
                        classGatherer.relevantClasses
                        );
        
        {//TODO
            System.out.printf("collected [%d] classes\n", classGatherer.relevantClasses.size());//TODO
            for(Class<?> c:classGatherer.relevantClasses){
                System.out.printf("\t%s\n", c.getName());
            }
        }//TODO
        
        Map<Class<?>,Set<Class<?>>> parentsToChildren = 
                getParentsToChildren(classGatherer.relevantClasses);
        
        for(Class<?> c:classGatherer.relevantClasses){
            generator.generateAnnotationClass(c);
        }
    }
    
    private AnnotationGenerator(
            AnnotationGeneratorConfiguration configuration,
            Set<Class<?>> relevantClasses
            ){
        this.configuration = configuration;
        this.parentsToChildren = getParentsToChildren(relevantClasses);
        this.childrenToParents = getChildrenToParents(relevantClasses);
    }
    
    private static Iterator<Field> fieldIterator(Class<?> c){
        List<Field> fields = new ArrayList<>();
        
        boolean stop = false;
        while(!stop){
            if(c == null){// || c == Object.class){
                stop = true;
                continue;
            }
            
            for(Field f:c.getDeclaredFields()){
                if(f.isSynthetic()){
                    continue;
                }
                fields.add(f);
            }
            
            c = c.getSuperclass();
        }
        
        return fields.iterator();
    }
    
    private static boolean isPrimitive(Class<?> c){
        return c.isPrimitive() || c.isEnum() || primitives.contains(c);
    }
    
    private static String getAnnotationPackageName(
            final String packagePrefix,
            Class<?> c
            ){
        final String typeName = getAnnotationTypeName(packagePrefix,c);
        
        String packageName = typeName;
        
        packageName = 
                packageName.substring(
                        0, 
                        packageName.lastIndexOf(c.getSimpleName())-1
                        );
        
        return packageName;
    }
    
    private static String getAnnotationTypeName(
            final String packagePrefix,
            Class<?> c
            ){
        if(isPrimitive(c)){
            return c.getCanonicalName();
        }
        
        if(c.isArray()){
            final int dim = 1 + c.getName().lastIndexOf('[');
            
            if(dim > 1){
                throw new RuntimeException(
                        "arrays with dim > 1 not supported, found " + c.getName());
            }
        }
        
        if(c.isArray() && isPrimitive(c.getComponentType())){
            return c.getCanonicalName();
        }
        
        final String elementName = 
                c.isArray() ? c.getComponentType().getCanonicalName() : c.getCanonicalName();
                
        if(elementName.contains("$")){
            throw new RuntimeException(elementName);//TODO
        }
        
        StringBuilder commonPrefix = new StringBuilder();
        String[] parts1 = packagePrefix.split("\\.");
        String[] parts2 = elementName.split("\\.");
        
        //com.securboration.immortals.ontology.functionality.FunctionalAspect
        //com.securboration.immortals.adsl.FunctionalAspect
        
        for(int i=0;i<parts1.length && i<parts2.length;i++){
            if(parts1[i].equals(parts2[i])){
                commonPrefix.append(parts1[i]).append(".");
            } else {
                break;
            }
        }
        
        if(commonPrefix.length() > 0){
            commonPrefix.deleteCharAt(commonPrefix.length()-1);
        }
        
        return packagePrefix + c.getCanonicalName().replace(commonPrefix.toString(), "").replace("com.securboration.immortals", "");
    }
    
    private void generateSemanticLinks(
            StringBuilder sb,
            Class<?> c
            ){
        
        sb.append("\n");
        
//        sb.append("  ").append("public String GUID() default \"\";\n");
        
        sb.append("/* begin:[FOR IMMORTALS TOOLING USE ONLY] */");
        
        final String uri = OntologyHelper.makeUriName(
                configuration.getJavaToTriplesConfiguration(), 
                Type.getType(c).getClassName());
        
        sb.append("\n");
        sb.append("  ").append("public static final String SEMANTIC_URI=\"").append(uri).append("\";\n");
        sb.append("  ").append("public static final String SEMANTIC_VERSION=\"").append("r2.0.0").append("\";\n");
        sb.append("  ").append("public static final long GEN_EPOCH_TIMESTAMP=").append(""+timestamp).append("L;\n");
        
        appendClass(sb,c);
        
        sb.append("/* end:[FOR IMMORTALS TOOLING USE ONLY] */\n");
    }
    
    private String getAnnotationOutputFile(
            Class<?> c
            ) throws IOException{
        final String outputDir = configuration.getAnnotationsOutputDir();
        final String packagePrefix = configuration.getPackagePrefix();
        final String packageName = getAnnotationPackageName(packagePrefix,c);
        
        return 
                new File(outputDir).getCanonicalPath() + "/" +
                packageName.replace(".", "/")+
                ("/")+
                (c.getSimpleName())+
                ".java"
                ;
    }
    
    private void generateAnnotationClass(Class<?> c) throws IOException{
        
        if(configuration.getAnnotationsOutputDir() == null){
            return;
        }
        
        if(c.isEnum()){
            //For now, enums are referenced directly on the classpath without 
            // conversion into an annotation type
            return;
        }
        
        final String sourceCode = 
                generateAnnotationClassInternal(c);
        
        final String outputPath = 
                getAnnotationOutputFile(c);
        
        FileUtils.writeStringToFile(
                new File(outputPath), 
                sourceCode
                );
    }
    
    
    
    private static void appendBoilerplateComments(
            StringBuilder sb,
            Class<?> source
            ){
        sb.append("/* WARNING: auto-generated content */\n");
        sb.append(String.format("/* generated from class %s on %s */\n",source.getName(),new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z").format(new Date())));
    }
    
    private static void appendPackage(
            StringBuilder sb,
            String packagePrefix,
            Class<?> source
            ){
        sb.append("\n");
        sb.append("package " + getAnnotationPackageName(packagePrefix,source) +";\n\n\n");
    }
    
    private void generateEnum(
            StringBuilder sb,
            Class<?> c
            ){
        sb.append("public enum ");
        sb.append(c.getSimpleName());
        sb.append("{\n");
        
        AtomicBoolean first = new AtomicBoolean(true);
        fieldIterator(c).forEachRemaining((f)->{
            
            if(f.getName().equals("$VALUES")){
                return;
            }
            
            if(f.getName().equals("name")){
                return;
            }
            
            if(f.getName().equals("ordinal")){
                return;
            }
            
            sb.append("  ");
            if(first.get()){
                first.set(false);
                sb.append(" ");
            } else {
                sb.append(",");
            }
            
            sb.append(f.getName()).append("\n");
        });
        
        sb.append(";\n\n");
        
        generateSemanticLinks(sb,c);
        
        sb.append("}\n");
    }
    
    private Set<Class<?>> getParentsOf(Class<?> c){
        if(!childrenToParents.containsKey(c)){
            throw new RuntimeException("unknown class: " + c.getName());
        }
        
        return childrenToParents.get(c);
    }
    
    private Set<Class<?>> getChildrenOf(Class<?> c){
        if(!parentsToChildren.containsKey(c)){
            throw new RuntimeException("unknown class: " + c.getName());
        }
        
        return parentsToChildren.get(c);
    }
    
    private void generateAnnotation(
            StringBuilder sb,
            Class<?> c
            ){
        final String packagePrefix = configuration.getPackagePrefix();
        
        sb.append("public @interface ");
        sb.append(c.getSimpleName());
        sb.append("{\n");
        
        Map<String,Class<?>> fieldsToTypes = new HashMap<>();
        
        fieldIterator(c).forEachRemaining((f)->{
            
            Class<?> elementType = f.getType();
            
            if(elementType.isEnum()){
                elementType = String.class;
            }
            
            if(elementType.isArray()){
                elementType = elementType.getComponentType();
            }
            
            if(elementType.equals(Class.class)){
                String genericName = f.getGenericType().getTypeName();
                genericName = genericName.replace("$", ".");
                
                sb.append("  public " + genericName + " " + f.getName() + "()");
            } else {
                
//                if(isPrimitive(elementType)){
//                    //do nothing
//                } else {
//                    Set<Class<?>> possibleTypes = 
//                            getChildrenOf(elementType);
//                    if(possibleTypes.size() > 0){
//                        StringBuilder s = new StringBuilder();
//                        s.append(String.format("\nin type %s, found polymorphic field \"%s\" with declared type %s and candidates:\n", c.getName(), f.getName(), elementType.getName()));
//                        possibleTypes.forEach(t->{
//                            s.append(String.format("\t%s\n", t.getName()));
//                        });
//                        s.append("\nBuffer state just before error:\n");
//                        s.append(sb.toString());
//                        
//                        throw new RuntimeException(s.toString());//TODO
//                    }
//                }
                
                sb.append("  public " + getAnnotationTypeName(packagePrefix,f.getType()) + " " + f.getName() + "()");
            }
            
            final String defaultValue = Defaults.getDefaultValueString(f);
            if(defaultValue != null){
                sb.append(" default " ).append(defaultValue);
            }
            else {
                sb.append(" default @" ).append(getAnnotationTypeName(packagePrefix,f.getType()));
                
//                System.out.printf("no default value for field \"%s\" with type %s and generic type %s\n", f.getName(), f.getType().getName(), f.getGenericType());
            }
//            else {
//                throw new RuntimeException(
//                        String.format("no default value for field \"%s\" with type %s\n\n%s\n", f.getName(), f.getType().getName(),sb.toString()));
//            }
            sb.append(";\n");
        });
        
        generateSemanticLinks(sb,c);
        
        sb.append("}\n");
    }
    
    private void appendMetaAnnotations(StringBuilder sb){
        sb.append("@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)\n");
    }
    
    private void appendClass(StringBuilder sb,Class<?> c){
        sb.append("  public static final Class<?> BACKING_POJO = " + c.getCanonicalName() + ".class;\n");
    }
    
    private String generateAnnotationClassInternal(
            Class<?> c
            ){
        final String packagePrefix = configuration.getPackagePrefix();
        
//        fieldIterator(c).forEachRemaining((f)->System.out.printf(
//                "found field: %s (name=%s) (simpleName=%s) (typeName=%s) (isPrimitive=%s) (generic=%s)\n", 
//                f.getName(), 
//                f.getType().getName(), 
//                f.getType().getSimpleName(),
//                f.getType().getTypeName(),
//                f.getType().isPrimitive(),
//                f.getGenericType().getTypeName()
//                ));
        
        StringBuilder sb = new StringBuilder();
        
        appendBoilerplateComments(sb,c);
        appendPackage(sb,packagePrefix,c);
        appendMetaAnnotations(sb);
        
        if(c.isEnum()){
            generateEnum(sb,c);
        } else {
            generateAnnotation(sb,c);
        }
        
        return sb.toString();
    }
    
    private static List<Class<?>> getTypeHierarchy(Class<?> c){
        
        List<Class<?>> hierarchy = new ArrayList<>(); 
        if(c == Object.class){
            return hierarchy;
        }
        
        boolean stop = false;
        while(!stop){
           hierarchy.add(c);
            
            c = c.getSuperclass();
            
            if(c == null || c == Object.class){
                stop = true;
            }
        }
        
        return hierarchy;
    }
    
    private static Map<Class<?>,Set<Class<?>>> getChildrenToParents(Collection<Class<?>> knownClasses){
        Map<Class<?>,Set<Class<?>>> map = new HashMap<>();
        
        for(Class<?> c:knownClasses){
            map.put(c, new HashSet<>(getTypeHierarchy(c)));
        }
        
        return map;
    }
    
    private static Map<Class<?>,Set<Class<?>>> getParentsToChildren(
            Collection<Class<?>> knownClasses
            ){
        Map<Class<?>,Set<Class<?>>> map = new HashMap<>();
        
        for(Class<?> c:knownClasses){
            for(Class<?> cParent:getTypeHierarchy(c)){
                Set<Class<?>> childrenForParent = map.get(cParent);
                
                if(childrenForParent == null){
                    childrenForParent = new HashSet<>();
                    map.put(cParent, childrenForParent);
                }
                
                childrenForParent.add(c);
            }
        }
        
        return map;
    }
    
    private static class ClassGathererVisitor implements ClasspathVisitor{
        
        private Set<Class<?>> relevantClasses = new HashSet<>();
        private Set<Class<?>> knownClasses = new HashSet<>();

        @Override
        public void visitClass(Class<?> c) {
            
            knownClasses.add(c);
            
            if(!AnnotationHelper.containsAnnotation(c, GenerateAnnotation.class)){
                return;
            }
            
            System.out.println(
                "discovered an @GenerateAnnotation class: " + c.getName());
            
            collectFieldTypes(c);
        }
        
        
        
        private void collectParents(Class<?> c){
            getTypeHierarchy(c).forEach(parent->{
                collectFieldTypes(parent);
                relevantClasses.add(parent);
            });
        }
        
        private void collectFieldTypes(Class<?> c){
            collectFieldTypes(c,new HashSet<>());
        }
        
//        private void TODO(){}
        
        private void collectFieldTypes(
                Class<?> c,
                Set<Class<?>> visitedAlready
                ){
            if(c.isArray()){
                c = c.getComponentType();
            }
            
            if(visitedAlready.contains(c)){
                //we've already seen this type, do nothing
                return;
            }
            visitedAlready.add(c);//TODO
            
            if(isPrimitive(c)){
                //it's a primitive type, do nothing
                return;
            }
            
            relevantClasses.add(c);
            
            if(c.isEnum()){
                //it's an enum type, don't dive into fields
                return;
            }
            
            //dive recursively into all field types
            fieldIterator(c).forEachRemaining(f->{
                collectFieldTypes(f.getType(),visitedAlready);
            });
        }

        @Override
        public void visitEnd() {
            final Map<Class<?>,Set<Class<?>>> classTree = 
                    getChildrenToParents(knownClasses);
            
            Set<Class<?>> alreadyAdded = new HashSet<>();
            
            boolean stop = false;
            while(!stop){
                Set<Class<?>> beforeClasses = new HashSet<>(relevantClasses);
                
                //process existing knowledge
                //TODO: memoize previously processed knowledge to prevent waste
                for(Class<?> c:new ArrayList<>(relevantClasses)){
                    
                    //don't re-visit a class we already know is relevant
                    {
                        if(alreadyAdded.contains(c)){
                            continue;
                        }
                        alreadyAdded.add(c);
                    }
                    
                    collectFieldTypes(c);
                    collectParents(c);
                }
                
                //print classes added via field/parent collection
                {
                    Set<Class<?>> afterClasses = new HashSet<>(relevantClasses);
                    afterClasses.removeAll(beforeClasses);
                    
                    for(Class<?> c:afterClasses){
                        System.out.printf("[field/parent collection]>\tadded class %s\n", c.getName());
                    }
                }
                
                //grow
                for(Class<?> knownClass:knownClasses){
                    for(Class<?> knownClassParent:classTree.get(knownClass)){
                        if(relevantClasses.contains(knownClassParent)){
                            relevantClasses.add(knownClass);
                        }
                    }
                }
                
                //print classes added via class tree growth
                {
                    Set<Class<?>> afterClasses = new HashSet<>(relevantClasses);
                    afterClasses.removeAll(beforeClasses);
                    
                    for(Class<?> c:afterClasses){
                        System.out.printf("[class tree growth]>\tadded class %s\n", c.getName());
                    }
                }
                 
                //print iteration statistics
                {
                    System.out.printf(
                        "before this iteration: %d, after: %d\n", 
                        beforeClasses.size(),
                        relevantClasses.size()
                        );
                }
                
                //loop until no new classes found
                if(relevantClasses.size() == beforeClasses.size()){
                    stop = true;
                }
            }
        }
    }
    
}
