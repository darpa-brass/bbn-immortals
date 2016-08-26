package com.securboration.immortals.instantiation.delete;
//package com.securboration.immortals.instantiation;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.ArrayList;
//import java.util.Base64;
//import java.util.List;
//
//import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
//import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
//import org.apache.commons.io.IOUtils;
//import org.objectweb.asm.ClassReader;
//import org.objectweb.asm.Type;
//import org.objectweb.asm.tree.AnnotationNode;
//import org.objectweb.asm.tree.ClassNode;
//import org.objectweb.asm.tree.FieldNode;
//import org.objectweb.asm.tree.MethodNode;
//
//import com.securboration.immortals.ontology.bytecode.AClass;
//import com.securboration.immortals.ontology.bytecode.AField;
//import com.securboration.immortals.ontology.bytecode.AMethod;
//import com.securboration.immortals.ontology.bytecode.AnAnnotation;
//import com.securboration.immortals.ontology.bytecode.AnnotationKeyValuePair;
//import com.securboration.immortals.ontology.bytecode.BytecodeArtifact;
//import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
//import com.securboration.immortals.ontology.bytecode.ClassStructure;
//import com.securboration.immortals.ontology.bytecode.JarArtifact;
//import com.securboration.immortals.ontology.bytecode.Modifier;
//
///**
// * Ingests a binary bytecode artifact
// * 
// * @author jstaples
// *
// */
//public class JarIngestor {
//    
//    private static void log(String format,Object...args){
//        System.out.println(String.format(format, args));//TODO
//    }
//    
//    
//    
//    public static BytecodeArtifact ingest(
//            byte[] jar,
//            String groupId,
//            String artifactId,
//            String version
//            ) throws IOException{
//        
//        BytecodeArtifact artifact = new JarArtifact();
//        
//        artifact.setBinaryForm(jar);
//        artifact.setCoordinate(new BytecodeArtifactCoordinate());
//        
//        artifact.getCoordinate().setGroupId(groupId);
//        artifact.getCoordinate().setArtifactId(artifactId);
//        artifact.getCoordinate().setVersion(version);
//        
//        openJar(new ByteArrayInputStream(jar),artifact);
//        
//        return artifact;
//    }
//    
//    private static ClassNode getClassNode(byte[] bytecode){
//        ClassReader cr = new ClassReader(bytecode);
//        ClassNode cn = new ClassNode();
//
//        cr.accept(cn, 0);// 0 = Don't expand frames or compute stack/local
//                         // mappings
//        
//        return cn;
//    }
//    
//    private static String pointerForField(AClass owner, FieldNode field){
//        return owner.getBytecodePointer() + "/fields/" + field.name;
//    }
//    
//    private static String pointerForMethod(AClass owner, MethodNode method){
//        return owner.getBytecodePointer() + "/methods/" + method.name + method.desc;
//    }
//    
//    private static AClass processClass(String hash,ClassNode cn){
//        
//        AClass aclass = new AClass();
//        addAnnotationModel(aclass,cn.visibleAnnotations);
//        addAnnotationModel(aclass,cn.invisibleAnnotations);
//        aclass.setBytecodePointer(hash);
//        aclass.setClassName(cn.name);
//        aclass.setModifiers(Modifier.getModifiers(cn.access));
//        
//        analyze(cn,aclass);
//        
//        return aclass;
//    }
//    
//    private static void addKeyValueToAnnotation(
//            AnAnnotation annotation,
//            AnnotationKeyValuePair kv
//            ){
//        
//        //TODO: painfully inefficient
//        AnnotationKeyValuePair[] oldKvs = annotation.getKeyValuePairs();
//        if(oldKvs == null){
//            oldKvs = new AnnotationKeyValuePair[]{};
//        }
//        
//        AnnotationKeyValuePair[] newKvs = 
//                new AnnotationKeyValuePair[oldKvs.length+1];
//        
//        System.arraycopy(oldKvs, 0, newKvs, 0, oldKvs.length);
//        
//        annotation.setKeyValuePairs(newKvs);
//        
//        newKvs[newKvs.length-1] = kv;
//    }
//    
//    private static AnnotationKeyValuePair createKeyValuePair(String key,Object value){
//        AnnotationKeyValuePair newKv = new AnnotationKeyValuePair();
//        newKv.setKey(key);
//        newKv.setValue(value);
//        
//        return newKv;
//    }
//    
//    private static void addAnnotationKeyValues(String key,Object value,AnAnnotation current){
//        
//        if(value instanceof AnnotationNode){
//            //it's an annotation on an annotation
//            
//            AnnotationNode a = (AnnotationNode)value;
//            
//            AnAnnotation annotation = new AnAnnotation();
//            annotation.setAnnotationClassName(
//                    Type.getType(a.desc).getClassName());
//            
//            List<Object> values = new ArrayList<>();
//            if(a.values != null){
//                values = a.values;
//            }
//            
//            for(int i=0;i<values.size()/2;i+=2){
//                String k = (String)values.get(i);
//                Object v = values.get(i+1);
//                
//                addAnnotationKeyValues(k,v,annotation);
//            }
//            
//            addKeyValueToAnnotation(
//                    current,
//                    createKeyValuePair(
//                            key,
//                            annotation));
//            
//            return;
//        } else if(value.getClass().isArray()){
//            
//            //if it's an array, the value part is an enumeration value
//            // v[0] is the descriptor of the enum
//            // v[1] is the string value of the enum selected
//            
//            addKeyValueToAnnotation(
//                    current,
//                    createKeyValuePair(
//                            key,
//                            ((Object[])value)[1]));
//            
//            return;
//        } else if(value instanceof List){
//            for(Object o:(List<?>)value){
//                addAnnotationKeyValues(key,o,current);
//            }
//            return;
//        } else if(value instanceof Type){
//            
//            addKeyValueToAnnotation(
//                    current,
//                    createKeyValuePair(
//                            key,
//                            ((Type)value).getClassName()));
//            
//            return;
//        } else {
//        
//            addKeyValueToAnnotation(
//                    current,
//                    createKeyValuePair(key,value));
//        
//            return;
//        }
//    }
//    
//    private static void addAnnotationModel(ClassStructure c,List<AnnotationNode> annotations){
//        
//        if(annotations == null){
//            return;
//        }
//        
//        List<AnAnnotation> annotationModels = new ArrayList<>();
//        
//        for(AnnotationNode annotation:annotations){
//            AnAnnotation a = new AnAnnotation();
//            
//            a.setAnnotationClassName(
//                    Type.getType(annotation.desc).getClassName());
//            
//            List<Object> values = new ArrayList<>();
//            if(annotation.values != null){
//                values = annotation.values;
//            }
//            
//            for(int i=0;i<values.size()/2;i+=2){
//                String key = (String)values.get(i);
//                Object value = values.get(i+1);
//                
//                addAnnotationKeyValues(key,value,a);
//            }
//            
//            annotationModels.add(a);
//        }
//        
//        c.setAnnotations(annotationModels.toArray(new AnAnnotation[]{}));
//        
//    }
//    
//    private static void analyze(ClassNode cn,AClass classModel){
//        
//        List<AField> fields = new ArrayList<>();
//        for(FieldNode fn:cn.fields){
//            
//            AField field = new AField();
//            field.setModifiers(Modifier.getModifiers(fn.access));
//            field.setFieldDesc(fn.desc);
//            field.setFieldName(fn.name);
//            field.setBytecodePointer(pointerForField(classModel,fn));
//            field.setOwner(classModel);
//            fields.add(field);
//            
//            addAnnotationModel(field,fn.visibleAnnotations);
//            addAnnotationModel(field,fn.invisibleAnnotations);
//        }
//        
//        List<AMethod> methods = new ArrayList<>();
//        for(MethodNode mn:cn.methods){
//            
//            AMethod method = new AMethod();
//            method.setModifiers(Modifier.getModifiers(mn.access));
//            method.setMethodDesc(mn.desc);
//            method.setMethodName(mn.name);
//            method.setBytecodePointer(pointerForMethod(classModel,mn));
//            method.setOwner(classModel);
//            methods.add(method);
//            
//            addAnnotationModel(method,mn.visibleAnnotations);
//            addAnnotationModel(method,mn.invisibleAnnotations);
//        }
//        
//        classModel.setFields(fields.toArray(new AField[]{}));
//        classModel.setMethods(methods.toArray(new AMethod[]{}));
//        
////        System.out.printf("\t[%d] fields and [%d] methods for [%s]\n", cn.fields.size(),cn.methods.size(),cn.name);
//    }
//    
//    private static String hash(byte[] data){
//        MessageDigest md;
//        try {
//            md = MessageDigest.getInstance("SHA-256");
//            md.update(data);
//            
//            return Base64.getEncoder().encodeToString(md.digest());
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//        
//        
//    }
//
//    private static void openJar(InputStream jarWithDependenciesPath,BytecodeArtifact artifact) throws IOException{
//        log("indexing jar...\n", jarWithDependenciesPath);
//
//        List<AClass> classes = new ArrayList<>();
//        
//        try(JarArchiveInputStream inJar = new JarArchiveInputStream(jarWithDependenciesPath);)
//        {
//            final int MAX_SIZE = 
//                    1024*1024//1MB
//                    *64;
//            
//            byte[] buffer = new byte[MAX_SIZE];
//    
//            // extract everything from the jar
//            boolean stop = false;
//            while (!stop) {
//                JarArchiveEntry jarEntry = inJar.getNextJarEntry();
//    
//                if (jarEntry == null) {
//                    stop = true;
//                } else {
//                    if (jarEntry.getSize() > MAX_SIZE) {
//                        throw new RuntimeException("jar entry too large, > " + MAX_SIZE);
//                    } else if (jarEntry.getSize() == 0) {
//                        // do nothing, the entry is not a file
//                    } else {
//                        final int length = IOUtils.read(inJar, buffer);
//                        byte[] jarContent = new byte[length];
//    
//                        System.arraycopy(buffer, 0, jarContent, 0, length);
//    
//                        if(jarEntry.getName().endsWith(".jar")) {
//                            log("found a nested jar: " + jarEntry.getName());
//                            
//                            //it's a nested jar, recurse
//                            openJar(
//                                    new ByteArrayInputStream(jarContent),
//                                    artifact);
//                        } else if (jarEntry.getName().endsWith(".class")) {
//                            log("found a class: " + jarEntry.getName());
//                            
//                            //it's a class, process it
//                            AClass aClass = 
//                                    processClass(
//                                            hash(jarContent),
//                                            getClassNode(jarContent));
//                            
//                            classes.add(aClass);
//                        }
//                    }
//                }
//            }
//        }
//        
//        artifact.setClasses(classes.toArray(new AClass[]{}));
//    }
//
//}
