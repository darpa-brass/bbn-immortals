package com.securboration.immortals.instantiation.bytecode;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.w3c.dom.Document;

import com.securboration.immortals.instantiation.bytecode.SourceFinder.SourceInfo;
import com.securboration.immortals.instantiation.bytecode.printing.MethodPrinter;
import com.securboration.immortals.o2t.etc.ExceptionWrapper;
import com.securboration.immortals.ontology.bytecode.AClass;
import com.securboration.immortals.ontology.bytecode.AField;
import com.securboration.immortals.ontology.bytecode.AMethod;
import com.securboration.immortals.ontology.bytecode.AnAnnotation;
import com.securboration.immortals.ontology.bytecode.AnnotationKeyValuePair;
import com.securboration.immortals.ontology.bytecode.BytecodeArtifact;
import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
import com.securboration.immortals.ontology.bytecode.BytecodeVersion;
import com.securboration.immortals.ontology.bytecode.ClassStructure;
import com.securboration.immortals.ontology.bytecode.JarArtifact;
import com.securboration.immortals.ontology.bytecode.Modifier;

/**
 * Ingests a binary bytecode artifact
 * 
 * @author jstaples
 *
 */
public class JarIngestor {
    
    private static final Logger logger = 
            LogManager.getLogger(JarIngestor.class);
    
    private static final boolean SKIP_ANNOTATIONS = false;//TODO
    private static final boolean INCLUDE_BYTECODE = false;//TODO
    
    private static void log(String format,Object...args){
        System.out.println(String.format(format, args));//TODO
    }
    
    private JarIngestor(final SourceFinder s,final AnnotationsToTriples a){
        this.sourceFinder = s;
        this.annotationsToTriples = a;
    }
    
    private final SourceFinder sourceFinder;
    
    private final AnnotationsToTriples annotationsToTriples;
    
    
    public static BytecodeArtifactCoordinate getCoordinate(File jar){
        final BytecodeArtifactCoordinate c = new BytecodeArtifactCoordinate();
        
        ExceptionWrapper.wrap(()->{
            
            final String jarPath = jar.getAbsolutePath();
            
            final String pomPath = 
                    jarPath.substring(
                            0,
                            jarPath.lastIndexOf(".jar")
                            ) + ".pom";
            
            if(!new File(pomPath).exists()){
                return;
            }
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(pomPath));
            XPath xPathFactory = XPathFactory.newInstance().newXPath();
            
            final String groupId = 
                    (String)xPathFactory.evaluate(
                            "/project/groupId",
                            document.getDocumentElement(),
                            XPathConstants.STRING
                            );
            final String artifactId = 
                    (String)xPathFactory.evaluate(
                            "/project/artifactId",
                            document.getDocumentElement(),
                            XPathConstants.STRING
                            );
            final String version = 
                    (String)xPathFactory.evaluate(
                            "/project/version",
                            document.getDocumentElement(),
                            XPathConstants.STRING
                            );
            
            c.setGroupId(groupId);
            c.setArtifactId(artifactId);
            c.setVersion(version);
        });
        
        return c;
    }
    
    public static BytecodeArtifact ingest(
            File dirContainingClasses,
            SourceFinder sourceFinder,
            AnnotationsToTriples annotationsToTriples
            ) throws IOException {

        JarArtifact artifact = new JarArtifact();

        JarIngestor ingestor = 
                new JarIngestor(sourceFinder,annotationsToTriples);

        ingestor.openNakedClasspathDir(dirContainingClasses, artifact);
        
        return artifact;
    }
    
    /**
     * 
     * @param jar
     *            a jar to ingest. It may contain nested jars.
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
    public static BytecodeArtifact ingest(
            byte[] jar,
            String groupId,
            String artifactId,
            String version,
            SourceFinder sourceFinder,
            AnnotationsToTriples annotationsToTriples
            ) throws IOException{
        
        JarArtifact artifact = new JarArtifact();
        
//        artifact.setBinaryForm(jar);
        artifact.setCoordinate(new BytecodeArtifactCoordinate());
        
        artifact.getCoordinate().setGroupId(groupId);
        artifact.getCoordinate().setArtifactId(artifactId);
        artifact.getCoordinate().setVersion(version);
        
        JarIngestor ingestor = 
                new JarIngestor(sourceFinder,annotationsToTriples);
        
        ingestor.openJar(
                new ByteArrayInputStream(jar),
                artifact
                );
        
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
    
    private static String pointerForMethod(AClass owner, MethodNode method){
        return owner.getBytecodePointer() + "/methods/" + method.name + method.desc;
    }
    
    private AClass processClass(String hash,ClassNode cn){
        
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
        analyze(cn,aclass);
        
        annotationsToTriples.visitClass(aclass, cn);
        
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
            
            Map<String,Object> kvs = 
                    AnnotationsToTriples.getAnnotationKeyValues(a);
            
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
    
    private static void analyze(ClassNode cn,AClass classModel){
        
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
            
            AMethod method = new AMethod();
            
            if(INCLUDE_BYTECODE){
                method.setBytecode(getMethodBytecode(mn));
            }
            method.setModifiers(Modifier.getModifiers(mn.access));
            method.setMethodDesc(mn.desc);
            method.setMethodName(mn.name);
            method.setBytecodePointer(pointerForMethod(classModel,mn));
            method.setOwner(classModel);
            methods.add(method);
            
            addAnnotationModel(method,mn.visibleAnnotations);
            addAnnotationModel(method,mn.invisibleAnnotations);
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
    
    private static JarArtifact createNestedJar(JarArtifact parent){
        //TODO: O(N) insertion
        JarArtifact[] nestedJars = parent.getNestedJars();
        
        if(nestedJars == null){
            nestedJars = new JarArtifact[1];
            parent.setNestedJars(nestedJars);
        }
        
        if(nestedJars[nestedJars.length-1] != null){
            JarArtifact[] newNestedJars = 
                    Arrays.copyOf(nestedJars, nestedJars.length * 2);
            
            nestedJars = newNestedJars;
            parent.setNestedJars(nestedJars);
        }
        
        Integer index = null;
        for(int i=0;i<nestedJars.length;i++){
            if(nestedJars[i]==null){
                index = i;
                break;
            }
        }
        
        JarArtifact returnValue = new JarArtifact();
        nestedJars[index] = returnValue;
        
        return returnValue;
    }
    
    private void openNakedClasspathDir(
            File classpathRoot,
            JarArtifact artifact
            ) throws IOException{
        log("indexing classpath...\n", classpathRoot);

        List<AClass> classes = new ArrayList<>();
        
        for(File f:FileUtils.listFiles(classpathRoot, new String[]{"class"}, true)){
            byte[] bytecode = FileUtils.readFileToByteArray(f);
            
            AClass aClass = 
                    processClass(
                            hash(bytecode),
                            getClassNode(bytecode));
            
            classes.add(aClass);
            
            setVersionInfo(
                    bytecode,
                    aClass
                    );//TODO
        }
        
        artifact.setClasses(classes.toArray(new AClass[]{}));
    }

    private void openJar(
            InputStream jarWithDependenciesPath,
            JarArtifact artifact
            ) throws IOException{
        log("indexing jar...\n", jarWithDependenciesPath);

        List<AClass> classes = new ArrayList<>();
        
        try(JarArchiveInputStream inJar = new JarArchiveInputStream(jarWithDependenciesPath);)
        {
            final int MAX_SIZE = 
                    1024*1024//1MB
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
                        throw new RuntimeException("jar entry too large, > " + MAX_SIZE);
                    } else if (jarEntry.getSize() == 0) {
                        // do nothing, the entry is not a file
                    } else {
                        final int length = IOUtils.read(inJar, buffer);
                        byte[] jarContent = new byte[length];
    
                        System.arraycopy(buffer, 0, jarContent, 0, length);
    
                        if(jarEntry.getName().endsWith(".jar")) {
                            log("found a nested jar: " + jarEntry.getName());
                            
                            //it's a nested jar, recurse
                            openJar(
                                    new ByteArrayInputStream(jarContent),
                                    createNestedJar(artifact)
                                    );
                        } else if (jarEntry.getName().endsWith(".class")) {
//                            log("found a class: " + jarEntry.getName());
                            
                            //it's a class, process it
                            AClass aClass = 
                                    processClass(
                                            hash(jarContent),
                                            getClassNode(jarContent));
                            
                            classes.add(aClass);
                            
                            setVersionInfo(
                                    jarContent,
                                    aClass
                                    );//TODO
                        }
                    }
                }
            }
        }
        
        artifact.setClasses(classes.toArray(new AClass[]{}));
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
