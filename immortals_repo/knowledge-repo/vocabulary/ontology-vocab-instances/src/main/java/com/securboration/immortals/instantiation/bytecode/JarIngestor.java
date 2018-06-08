package com.securboration.immortals.instantiation.bytecode;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.w3c.dom.Document;

import com.securboration.immortals.instantiation.bytecode.SourceFinder.SourceInfo;
import com.securboration.immortals.instantiation.bytecode.printing.MethodPrinter;
import com.securboration.immortals.o2t.UniqueMethodCall;
import com.securboration.immortals.o2t.etc.ArrayHelper;
import com.securboration.immortals.o2t.etc.ExceptionWrapper;
import com.securboration.immortals.ontology.bytecode.AClass;
import com.securboration.immortals.ontology.bytecode.AField;
import com.securboration.immortals.ontology.bytecode.AMethod;
import com.securboration.immortals.ontology.bytecode.AnAnnotation;
import com.securboration.immortals.ontology.bytecode.AnnotationKeyValuePair;
import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
import com.securboration.immortals.ontology.bytecode.BytecodeVersion;
import com.securboration.immortals.ontology.bytecode.ClassArtifact;
import com.securboration.immortals.ontology.bytecode.ClassStructure;
import com.securboration.immortals.ontology.bytecode.ClasspathElement;
import com.securboration.immortals.ontology.bytecode.ClasspathResource;
import com.securboration.immortals.ontology.bytecode.InvocationType;
import com.securboration.immortals.ontology.bytecode.JarArtifact;
import com.securboration.immortals.ontology.bytecode.MethodArg;
import com.securboration.immortals.ontology.bytecode.Modifier;
import com.securboration.immortals.ontology.bytecode.analysis.FieldAccess;
import com.securboration.immortals.ontology.bytecode.analysis.Instruction;
import com.securboration.immortals.ontology.bytecode.analysis.MethodCall;
import com.securboration.immortals.ontology.bytecode.application.Classpath;

/**
 * Ingests a binary bytecode artifact
 * 
 * @author jstaples
 *
 */
public class JarIngestor {
    
    private static final Logger logger = 
            LogManager.getLogger(JarIngestor.class);
    
    static{
        logger.error(
            "This class will soon be deprecated in favor of " +
            "StaticBytecodeAnalyzer in the immortals-bytecode-analysis module");
    }
    
    private static final boolean SKIP_ANNOTATIONS = false;//TODO
    private static final boolean INCLUDE_BYTECODE = false;//TODO
    private static final boolean INCLUDE_BINARY = false;//TODO
    private static final boolean INCLUDE_INTERESTING_INSTRUCTIONS = true;
    
    private static void log(String format,Object...args){
        System.out.println(String.format(format, args));//TODO
    }
    
    public JarIngestor(final SourceFinder s) {
        this.sourceFinder = s;
    }
    
    private final SourceFinder sourceFinder;
    
    public static BytecodeArtifactCoordinate getCoordinate(File jar){
        final BytecodeArtifactCoordinate c = new BytecodeArtifactCoordinate();
        
        ExceptionWrapper.wrap(()->{
            
            final String jarPath = jar.getAbsolutePath();
            
            String pomPath = 
                    jarPath.substring(
                            0,
                            jarPath.lastIndexOf(".jar")
                            ) + ".pom";
            
            if(!new File(pomPath).exists()){

                final String findPomPath = jarPath.substring(
                        0,
                        jarPath.lastIndexOf(File.separatorChar));

                Collection<File> possPoms = FileUtils.listFiles(new File(findPomPath).getParentFile(), new String[]{"pom"}, true);
                if (possPoms.isEmpty()) {
                    return;
                }
                pomPath = possPoms.iterator().next().getAbsolutePath();
            }
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(pomPath));
            XPath xPathFactory = XPathFactory.newInstance().newXPath();
            
            String groupId = 
                    (String)xPathFactory.evaluate(
                            "/project/groupId",
                            document.getDocumentElement(),
                            XPathConstants.STRING
                            );
            
            if (groupId.equals("")) {
                groupId = (String)xPathFactory.evaluate(
                        "/project/parent/groupId",
                        document.getDocumentElement(),
                        XPathConstants.STRING
                );
            }
            
            final String artifactId = 
                    (String)xPathFactory.evaluate(
                            "/project/artifactId",
                            document.getDocumentElement(),
                            XPathConstants.STRING
                            );
            
            String version = 
                    (String)xPathFactory.evaluate(
                            "/project/version",
                            document.getDocumentElement(),
                            XPathConstants.STRING
                            );
            
            if (version.equals("")) {
                version = (String)xPathFactory.evaluate(
                        "/project/parent/version",
                        document.getDocumentElement(),
                        XPathConstants.STRING
                );
            }
            
            c.setGroupId(groupId);
            c.setArtifactId(artifactId);
            c.setVersion(version);
        });
        
        return c;
    }

    // Ingest single, naked class file
    public static ClasspathElement ingest(
            File classFile,
            SourceFinder sourceFinder
    ) throws IOException {

        JarIngestor ingestor = new JarIngestor(sourceFinder);
        
        ClasspathElement element;
        
        byte[] binary = FileUtils.readFileToByteArray(classFile);

        AClass aClass =
                ingestor.processClass(
                        hash(binary),
                        getClassNode(binary), false);

        setVersionInfo(
                binary,
                aClass
        );

        ClassArtifact c = new ClassArtifact();
        c.setClassModel(aClass);

        element = c;

        String name = classFile.getName();
        
        element.setHash(hash(binary));
        element.setName(name);
        
        return element;
    }
    
    public static Classpath ingestDirectory(
            File dirContainingClasses,
            SourceFinder sourceFinder
            ) throws IOException {
        
        JarIngestor ingestor = 
                new JarIngestor(sourceFinder);
        
        Classpath classpath = new Classpath();
        
        ingestor.openNakedClasspathDir(
            dirContainingClasses, 
            classpath
            );
        
        return classpath;
    }
    
    /**
     * 
     * @param jar
     *            a jar to ingest. It may contain nested jars.
     * @param name 
     *            the name of the jar
     * @param groupId
     *            the group portion of the coordinate
     * @param artifactId
     *            the artifact id portion of the coordinate
     * @param version
     *            the version portion of the coordinate
     * @param sourceFinder
     *            used to locate source code that compiled into this artifact
     * @return a model of the bytecode structure
     * @throws IOException
     */
    public static JarArtifact ingest(
            byte[] jar,
            String name,
            String groupId,
            String artifactId,
            String version,
            SourceFinder sourceFinder
            ) throws IOException{
        
        JarArtifact artifact = new JarArtifact();
        
        if(INCLUDE_BINARY){
            artifact.setBinaryForm(jar);
        }
        artifact.setHash(hash(jar));
        artifact.setName(name);
        
        artifact.setCoordinate(new BytecodeArtifactCoordinate());
        
        artifact.getCoordinate().setGroupId(groupId);
        artifact.getCoordinate().setArtifactId(artifactId);
        artifact.getCoordinate().setVersion(version);
        
        JarIngestor ingestor = 
                new JarIngestor(sourceFinder);
        
        ingestor.openJar(
                new ByteArrayInputStream(jar),
                artifact
                );
        
        return artifact;
    }

    public static JarArtifact ingest(
            byte[] jar,
            String name,
            String groupId,
            String artifactId,
            String version,
            SourceFinder sourceFinder,
            boolean fullAnalysis
            
    ) throws IOException{

        JarArtifact artifact = new JarArtifact();

        if(INCLUDE_BINARY){
            artifact.setBinaryForm(jar);
        }
        artifact.setHash(hash(jar));
        artifact.setName(name);

        artifact.setCoordinate(new BytecodeArtifactCoordinate());

        artifact.getCoordinate().setGroupId(groupId);
        artifact.getCoordinate().setArtifactId(artifactId);
        artifact.getCoordinate().setVersion(version);
        
        if (fullAnalysis) {
            JarIngestor ingestor =
                    new JarIngestor(sourceFinder);

            ingestor.openJar(
                    new ByteArrayInputStream(jar),
                    artifact
            );
        }
        return artifact;
    }
    
    
    private static ClassNode getClassNode(byte[] bytecode){
        ClassReader cr = new ClassReader(bytecode);
        ClassNode cn = new ClassNode();

        cr.accept(cn, 0);// 0 = Don't expand frames or compute stack/local
                         // mappings
        
        return cn;
    }
    
    private static String pointerForField(AClass owner, FieldNode field){
        return owner.getBytecodePointer() + "/fields/" + field.name;
    }
    
    public static String pointerForMethod(AClass owner, MethodNode method){
        return owner.getBytecodePointer() + "/methods/" + method.name + method.desc;
    }

    public static Map<String,Object> getAnnotationKeyValues(
            AnnotationNode a
    ){

        List<String> pathSoFar = new ArrayList<>();
        pathSoFar.add("@"+ Type.getType(a.desc).getInternalName());

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


    public AClass processClass(String hash,ClassNode cn, boolean userCode){
        
        AClass aclass = new AClass();
        addAnnotationModel(aclass,cn.visibleAnnotations);
        addAnnotationModel(aclass,cn.invisibleAnnotations);
        aclass.setBytecodePointer(hash);
        aclass.setClassName(cn.name);
        aclass.setModifiers(Modifier.getModifiers(cn.access));
        
        SourceInfo info = sourceFinder.getSourceInfo(cn.name);
        if(info != null){
            aclass.setClassUrl(info.getRepoUrl());
        } else {
            
            logger.info("no class info available for " + cn.name);
        }
        analyze(cn,aclass, userCode);
        
        return aclass;
    }
    
    private static void addAnnotationModel(
            ClassStructure c,
            List<AnnotationNode> annotations
            ){
        if(SKIP_ANNOTATIONS){
            return;
        }
        
        if(annotations == null){
            return;
        }
        
        List<AnAnnotation> annotationModels = new ArrayList<>();
        for(AnnotationNode a:annotations){
            
            AnAnnotation annotationModel = new AnAnnotation();
            annotationModel.setAnnotationClassName(
                    Type.getType(a.desc).getInternalName()
                    );
            List<AnnotationKeyValuePair> annotationKvs = new ArrayList<>();
            
            Map<String, Object> kvs = getAnnotationKeyValues(a);
            
            for(String key:new ArrayList<>(kvs.keySet())){
                Object value = kvs.get(key);
                
                if(value.getClass().isArray()){
                    //it's an array of primitives
                    
                    List<String> values = new ArrayList<>();
                    for(int i=0;i<Array.getLength(value);i++){
                        Object element = Array.get(value, i);
                        
                        values.add(String.format("%s",element));
                    }
                    
                    kvs.put(key, values);
                }
            }
            
            for(String key:new ArrayList<>(kvs.keySet())){
                final String value = String.format("%s",kvs.get(key));
                
                AnnotationKeyValuePair kv = new AnnotationKeyValuePair();
                kv.setKey(key);
                kv.setValue(value);
                
                annotationKvs.add(kv);
            } 
            
            annotationModel.setKeyValuePairs(
                    annotationKvs.toArray(
                            new AnnotationKeyValuePair[]{}));
            
            annotationModels.add(annotationModel);
        }
        
        List<AnAnnotation> annotationList = new ArrayList<>();
        if(c.getAnnotations() != null){
            annotationList.addAll(Arrays.asList(c.getAnnotations()));
        }
        annotationList.addAll(annotationModels);
        c.setAnnotations(annotationList.toArray(new AnAnnotation[]{}));
    }
    
    private static void addLocalAnalysis(
            AMethod methodModel, 
            ClassNode owner,
            MethodNode methodToAnalyze
            ){
        final List<ParameterNode> params = methodToAnalyze.parameters;
        final Type[] types = Type.getArgumentTypes(methodToAnalyze.desc);
        
        List<MethodArg> args = new ArrayList<>();
        
        int localIndex = 0;
        if((methodToAnalyze.access & Opcodes.ACC_STATIC) > 0){
            //it's a static method so there's no implicit "this" param
        } else {
            //it's an instance method, so there is an implicit "this" param
            MethodArg arg = new MethodArg();
            arg.setArgNumber(0);
            arg.setDeclaredTypeDescriptor(Type.getType(owner.name).getDescriptor());
            arg.setLocalIndex(localIndex);
            arg.setName("this");
            arg.setOwner(methodModel);
            
            args.add(arg);
            localIndex++;
        }
        
        int argNum = 1;
        for(Type type:types){
            MethodArg arg = new MethodArg();
            arg.setArgNumber(argNum);
            arg.setDeclaredTypeDescriptor(type.getDescriptor());
            arg.setLocalIndex(localIndex);
            arg.setName("incomplete parameter info in bytecode");
            if(params != null && argNum <= params.size()){
                ParameterNode p = params.get(argNum-1);
                if(p != null){
                    arg.setName(p.name);
                }
            }
            arg.setOwner(methodModel);
            
            args.add(arg);
            argNum++;
            localIndex+=type.getSize();
        }
        
        methodModel.setMethodArgs(args.toArray(new MethodArg[]{}));
    }
    
    private static void analyze(ClassNode cn,AClass classModel, boolean userCode) {
        
        List<AField> fields = new ArrayList<>();
        Set<String> dependentFiles = new HashSet<>();
        for(FieldNode fn:cn.fields){
            
            AField field = new AField();
            field.setModifiers(Modifier.getModifiers(fn.access));
            field.setFieldDesc(fn.desc);
            field.setFieldName(fn.name);
            field.setBytecodePointer(pointerForField(classModel,fn));
            field.setOwner(classModel);
            fields.add(field);
            
            addAnnotationModel(field,fn.visibleAnnotations);
            addAnnotationModel(field,fn.invisibleAnnotations);
        }
        
        List<AMethod> methods = new ArrayList<>();
        
        for(MethodNode mn:cn.methods){
            
            List<Instruction> interestingInstructions = new ArrayList<>();
            int methodCallOrder = 1;
            AbstractInsnNode[] instructions = mn.instructions.toArray();

            for (AbstractInsnNode ain : instructions) {

                if (ain.getType() == AbstractInsnNode.FIELD_INSN) {

                    FieldInsnNode fieldInsnNode = (FieldInsnNode) ain;
                    FieldAccess fieldAccess = new FieldAccess();

                    fieldAccess.setFieldDesc(fieldInsnNode.desc);
                    fieldAccess.setFieldName(fieldInsnNode.name);
                    
                    if (userCode) {
                        try {
                            Class clazz = Class.forName(fieldInsnNode.owner.replace('/', '.'));
                            URL location = clazz.getResource(("/" + fieldInsnNode.owner) + ".class");
                            dependentFiles.add(location.toString());
                        } catch (Exception exc) {
                        }
                    }
                    
                    fieldAccess.setFieldOwnerHash(hash(fieldInsnNode.owner.getBytes()));

                    if (INCLUDE_INTERESTING_INSTRUCTIONS)
                        interestingInstructions.add(fieldAccess);
                    

                } else if (ain.getType() == AbstractInsnNode.METHOD_INSN) {

                    MethodInsnNode methodInsnNode = (MethodInsnNode) ain;
                    int lineNumber = getLineNumber(methodInsnNode);
                    
                    MethodCall methodCall = new MethodCall();
                    
                    methodCall.setCalledMethodDesc(methodInsnNode.desc);
                    methodCall.setCalledMethodName(methodInsnNode.name);

                    if (userCode) {
                        try {
                            Class clazz = Class.forName(methodInsnNode.owner.replace('/', '.'));
                            URL location = clazz.getResource(("/" + methodInsnNode.owner) + ".class");
                            dependentFiles.add(location.toString());
                        } catch (Exception exc) {
                        }
                    }
                    
                    methodCall.setLineNumber(lineNumber);
                    methodCall.setOrder(methodCallOrder);
                    methodCallOrder++;

                    if (INCLUDE_INTERESTING_INSTRUCTIONS)
                        interestingInstructions.add(methodCall);
                }
            }

            AMethod method = new AMethod();
            
            if(INCLUDE_BYTECODE){
                method.setBytecode(getMethodBytecode(mn));
            }
            method.setModifiers(Modifier.getModifiers(mn.access));
            method.setMethodDesc(mn.desc);
            method.setMethodName(mn.name);
            method.setBytecodePointer(pointerForMethod(classModel,mn));
            method.setInterestingInstructions(interestingInstructions.toArray(
                    new Instruction[interestingInstructions.size()]));
            method.setOwner(classModel);
            methods.add(method);
            
            addAnnotationModel(method,mn.visibleAnnotations);
            addAnnotationModel(method,mn.invisibleAnnotations);
            addLocalAnalysis(method,cn,mn);
        }
        
        classModel.setFields(fields.toArray(new AField[]{}));
        classModel.setMethods(methods.toArray(new AMethod[]{}));
        
        String[] dependentFilesArray = new String[dependentFiles.size()];
        Iterator<String> fileIter = dependentFiles.iterator();
        for (int i = 0; i < dependentFilesArray.length; i++) {
            dependentFilesArray[i] = fileIter.next();
        }
        
        classModel.setDependentFiles(dependentFilesArray);
//        System.out.printf("\t[%d] fields and [%d] methods for [%s]\n", cn.fields.size(),cn.methods.size(),cn.name);
    }

    private static int getLineNumber(AbstractInsnNode node) {
        AbstractInsnNode currentNode = node;

        // First search backwards
        while (currentNode != null) {
            if (currentNode.getType() == AbstractInsnNode.LINE) {
                return ((LineNumberNode) currentNode).line;
            }
            currentNode = currentNode.getPrevious();
        }

        // Then search forwards
        currentNode = node;
        while (currentNode != null) {
            if (currentNode.getType() == AbstractInsnNode.LINE) {
                return ((LineNumberNode) currentNode).line;
            }
            currentNode = currentNode.getNext();
        }

        return -1;
    }

    private static void analyze(ClassNode cn,AClass classModel, HashMap<String, UniqueMethodCall> pointers){
        List<AField> fields = new ArrayList<>();
        for(FieldNode fn:cn.fields){

            AField field = new AField();
            field.setModifiers(Modifier.getModifiers(fn.access));
            field.setFieldDesc(fn.desc);
            field.setFieldName(fn.name);
            field.setBytecodePointer(pointerForField(classModel,fn));
            field.setOwner(classModel);
            fields.add(field);

            addAnnotationModel(field,fn.visibleAnnotations);
            addAnnotationModel(field,fn.invisibleAnnotations);
        }

        List<AMethod> methods = new ArrayList<>();
        
        for(MethodNode mn:cn.methods){
            int methodCallOrder = 1;
            List<Instruction> interestingInstructions = new ArrayList<>();
            AbstractInsnNode[] instructions = mn.instructions.toArray();

            for (AbstractInsnNode ain : instructions) {

                if (ain.getType() == AbstractInsnNode.FIELD_INSN) {

                    FieldInsnNode fieldInsnNode = (FieldInsnNode) ain;
                    FieldAccess fieldAccess = new FieldAccess();

                    fieldAccess.setFieldDesc(fieldInsnNode.desc);
                    fieldAccess.setFieldName(fieldInsnNode.name);
                    fieldAccess.setFieldOwnerHash(hash(fieldInsnNode.owner.getBytes()));

                    if (INCLUDE_INTERESTING_INSTRUCTIONS)
                        interestingInstructions.add(fieldAccess);
                    
                    
                } else if (ain.getType() == AbstractInsnNode.METHOD_INSN) {

                    MethodInsnNode methodInsnNode = (MethodInsnNode) ain;
                    MethodCall methodCall = new MethodCall();
                    
                    methodCall.setCalledMethodDesc(methodInsnNode.desc);
                    methodCall.setCalledMethodName(methodInsnNode.name);
                    methodCall.setOwner(methodInsnNode.owner);
                    methodCall.setInvocationType(InvocationType.getType(methodInsnNode.getOpcode()));
                    methodCall.setOrder(methodCallOrder);
                    methodCallOrder++;
                    
                    UniqueMethodCall testAgainstCache = new UniqueMethodCall(methodInsnNode.name, 
                            methodInsnNode.desc, methodInsnNode.owner);
                    
                    List<String> possPointers = new ArrayList<>();
                    
                    //TODO might be able to do this out in Project2Triples, also might not be worth the computation...
                    for (Map.Entry<String, UniqueMethodCall> entry : pointers.entrySet()) {
                        if (entry.getValue().equals(testAgainstCache)) {
                            possPointers.add(entry.getKey());
                        }
                    }
                    
                    if (possPointers.size() != 1) {
                        methodCall.setOwnerAssurance(false);
                    } else {
                        methodCall.setOwnerAssurance(true);
                    }
                    
                    if (INCLUDE_INTERESTING_INSTRUCTIONS)
                        interestingInstructions.add(methodCall);
                }
            }

            AMethod method = new AMethod();
            
            if(INCLUDE_BYTECODE){
                method.setBytecode(getMethodBytecode(mn));
            }
            method.setModifiers(Modifier.getModifiers(mn.access));
            method.setMethodDesc(mn.desc);
            method.setMethodName(mn.name);
            method.setBytecodePointer(pointerForMethod(classModel,mn));
            method.setInterestingInstructions(interestingInstructions.toArray(
                    new Instruction[interestingInstructions.size()]));
            method.setOwner(classModel);
            methods.add(method);
            
            addAnnotationModel(method,mn.visibleAnnotations);
            addAnnotationModel(method,mn.invisibleAnnotations);
            addLocalAnalysis(method,cn,mn);
        }
        
        classModel.setFields(fields.toArray(new AField[]{}));
        classModel.setMethods(methods.toArray(new AMethod[]{}));
        
//        System.out.printf("\t[%d] fields and [%d] methods for [%s]\n", cn.fields.size(),cn.methods.size(),cn.name);
    }
    
    private static String getMethodBytecode(MethodNode mn){
        
        if(mn.instructions == null){
            return null;
        } else if(mn.instructions.size() == 0){
            return null;
        }
        
        return MethodPrinter.print(mn);
    }
    
    private static String hash(byte[] data){
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            
            return Base64.getEncoder().encodeToString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static JarArtifact createNestedJar(
            JarArtifact parent
            ){
        
        JarArtifact child = new JarArtifact();
        
        //TODO: O(N) insertion
        parent.setJarContents(
            (ClasspathElement[])ArrayHelper.append(
                parent.getJarContents(), 
                ClasspathElement.class, 
                child
                )
            );
        
        return child;
    }
    
    private void openNakedClasspathDir(
            File classpathRoot,
            Classpath classpath
            ) throws IOException{
        
        log("indexing classpath...\n", classpathRoot);

        final String prefix = classpathRoot.getAbsolutePath();

        List<ClasspathElement> elements = new ArrayList<>();
        
        for(File f:FileUtils.listFiles(classpathRoot, new String[]{}, true)){
            
            byte[] binary = FileUtils.readFileToByteArray(f);
            
            final ClasspathElement e;
            
            if(f.getName().endsWith(".class")){
                //it's a class
                
                AClass aClass = 
                        processClass(
                                hash(binary),
                                getClassNode(binary), false);
                
                setVersionInfo(
                    binary,
                    aClass
                    );//TODO
                
                ClassArtifact c = new ClassArtifact();
                c.setClassModel(aClass);
                
                e = c;
            } else if(f.getName().endsWith(".jar")){
                //it's a jar
                
                JarArtifact j = new JarArtifact();
                
                openJar(new ByteArrayInputStream(binary),j);
                
                e = j;
            } else {
                //it's a resource
                
                e = new ClasspathResource();
            }
            
            if(INCLUDE_BINARY){
                e.setBinaryForm(binary);
            }
            
            String name = f.getAbsolutePath().substring(prefix.length());
            if(name.startsWith("/") || name.startsWith("\\")){
                name = name.substring(1);
            }
            
            e.setHash(hash(binary));
            e.setName(name);
            e.setName(f.getPath());
            elements.add(e);
        }
        
        classpath.setElement(elements.toArray(new ClasspathElement[]{}));
    }

    private void openJar(
            InputStream jarWithDependenciesPath,
            JarArtifact artifact
            ) throws IOException{
        log("indexing jar...\n", jarWithDependenciesPath);

        List<ClasspathElement> resources = new ArrayList<>();
        
        try(JarArchiveInputStream inJar = new JarArchiveInputStream(jarWithDependenciesPath);)
        {
            final int MAX_SIZE = 
                    1024*1024//1MiB
                    *64;
            
            byte[] buffer = new byte[MAX_SIZE];
    
            // extract everything from the jar
            boolean stop = false;
            while (!stop) {
                JarArchiveEntry jarEntry = inJar.getNextJarEntry();
    
                if (jarEntry == null) {
                    stop = true;
                } else {
                    if (jarEntry.getSize() > MAX_SIZE) {
                        throw new RuntimeException(
                            "jar entry too large, > " + MAX_SIZE);
                    } else if (jarEntry.isDirectory()) {
                        // do nothing, the entry is not a file
                    } else if (jarEntry.isUnixSymlink()) {
                        // do nothing, the entry is not a file
                    } else {
                        
                        final ClasspathElement element;
                        
                        final int length = IOUtils.read(inJar, buffer);
                        byte[] jarContent = new byte[length];
    
                        System.arraycopy(buffer, 0, jarContent, 0, length);
    
                        if(jarEntry.getName().endsWith(".jar")) {
                            log("found a nested jar: " + jarEntry.getName());
                            
                            JarArtifact j = createNestedJar(artifact);
                            
                            //it's a nested jar, recurse
                            openJar(new ByteArrayInputStream(jarContent),j);
                            
                            element = j;
                        } else if (jarEntry.getName().endsWith(".class")) {
                            //it's a class
                            
                            AClass aClass = 
                                    processClass(
                                            hash(jarContent),
                                            getClassNode(jarContent), false);
                            
                            setVersionInfo(
                                jarContent,
                                aClass
                                );//TODO
                            
                            ClassArtifact c = new ClassArtifact();
                            c.setClassModel(aClass);

                            element = c;
                        } else {
                            //it's a resource
                            
                            element = new ClasspathResource();
                        }
                        
                        // Embedded model, print and continue processing
                        if (jarEntry.getName().endsWith(".ttl")) {
                            
                            File file = new File(sourceFinder.getProjectRoot() + "/embeddedModels/" + jarEntry.getName());
                            file.getParentFile().mkdirs();
                            file.createNewFile();
                            
                            FileWriter fw = new FileWriter(file);
                            fw.write(new String(jarContent));
                            fw.flush();
                            fw.close();
                        } else {

                            element.setName(jarEntry.getName());
                            element.setHash(hash(jarContent));
                            if (INCLUDE_BINARY) {
                                element.setBinaryForm(jarContent);
                            }

                            resources.add(element);
                        }
                    }
                }
            }
        }
        
        artifact.setJarContents(resources.toArray(new ClasspathElement[]{}));
    }
    
    private static void setVersionInfo(
            byte[] bytecode,
            AClass aClass
            ) throws IOException {
        DataInputStream in = new DataInputStream(
                new ByteArrayInputStream(bytecode));

        final int magicBytes = in.readInt();
        if (magicBytes != 0xCAFE_BABE) {
            throw new RuntimeException(
                    "expected magic word 0xCAFE BABE but didn't find it");
        }

        final int minorVersion = 
                in.readUnsignedShort();
        final int majorVersion = 
                in.readUnsignedShort();
        final String javaVersionTag = 
                VersionMapper.getJavaVersionFromMajorVersion(majorVersion);
        
        BytecodeVersion version = new BytecodeVersion();
        version.setMajorVersionTag(""+majorVersion);
        version.setMinorVersionTag(""+minorVersion);
        version.setPlatformVersionTag(javaVersionTag);
        
        aClass.setBytecodeVersion(version);
    }
    
    private static class VersionMapper{
        private static final Map<Integer,String> map = new HashMap<>();
        
        static
        {
            map.put(0x34, "Java SE 8");
            map.put(0x33, "Java SE 7");
            map.put(0x32, "Java SE 6.0");
            map.put(0x31, "Java SE 5.0");
            map.put(0x30, "JDK 1.4");
            map.put(0x2F, "JDK 1.3");
            map.put(0x2E, "JDK 1.2");
            map.put(0x2D, "JDK 1.1");
        }
        
        private static String getJavaVersionFromMajorVersion(final int majorVersion){
            
            if(!map.containsKey(majorVersion)){
                throw new RuntimeException(
                        "major version " + majorVersion + " is unmapped");
            }
            
            return map.get(majorVersion);
            
        }
    }

}
