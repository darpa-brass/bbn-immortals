package com.securboration.immortals.bca;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.securboration.immortals.bcad.dataflow.AnalysisFilter;
import com.securboration.immortals.bcad.instrument.IBytecodeTransformerClass;
import com.securboration.immortals.bcad.instrument.IBytecodeTransformerMethod;
import com.securboration.immortals.helpers.ImmortalsPointerHelper;
import com.securboration.immortals.instantiation.annotationparser.bytecode.BytecodeHelper;
import com.securboration.immortals.instantiation.annotationparser.traversal.JarTraverser;

/**
 * A classloader that permits loading class resources from an analysis classpath
 * 
 * @author jstaples
 *
 */
public class AnalysisClassloader extends ClassLoader{
    
    private static final Logger logger = 
            LoggerFactory.getLogger(AnalysisClassloader.class);
    
    /*
     * Emulates the permgen heap region of the JVM
     * 
     * Map of class names to their corresponding Class models
     */
    private final Map<String,Class<?>> permgen = new HashMap<>();
    
    private final Map<String,byte[]> bytecodeMap = new HashMap<>();
    private final Map<String,String> classNameToClassHashMap = new HashMap<>();
    
    private final ClassLoader parent;
    
    private final List<IBytecodeTransformerClass> lazyTransformers = 
            new ArrayList<>();
    
    private AnalysisClassloader(ClassLoader parent){
        this.parent = parent;
    }
    
    public AnalysisClassloader(){
        this(AnalysisClassloader.class.getClassLoader());
    }
    
    public AnalysisClassloader copy(){
        AnalysisClassloader newLoader = new AnalysisClassloader(this.parent);
        
        newLoader.bytecodeMap.putAll(this.bytecodeMap);
        newLoader.classNameToClassHashMap.putAll(this.classNameToClassHashMap);
        newLoader.lazyTransformers.addAll(this.lazyTransformers);
        newLoader.permgen.putAll(this.permgen);
        
        return newLoader;
    }
    
    public Set<String> getLoadedClassHashes(){
        Set<String> loaded = new HashSet<>();
        
        for(final String name:permgen.keySet()){
            final String hash = classNameToClassHashMap.get(name);
            loaded.add(hash);
        }
        
        return loaded;
    }
    
    public String getNameFromHash(final String classHash){
        for(final String name:classNameToClassHashMap.keySet()){
            final String hash = classNameToClassHashMap.get(name);
            
            if(hash.equals(classHash)){
                return name;
            }
        }
        
        throw new RuntimeException("no match found for hash " + classHash);
    }
    
    public Class<?> getClassWithHash(final String classHash){
        return permgen.get(getNameFromHash(classHash));
    }
    
    public Method getMethodFromHash(final String methodHash){
        
        //e.g.,
        //tCDY1SFM+HtvIT75KyY5rbtg5i/qbCGzFiN7QdxQInw=/methods/formatLongOctalBytes(J[BII)I
        //class hash                                 XXXXXXXXXX method signature
        final String delimiter = "=/methods/";
        final String classHash = methodHash.substring(0,methodHash.indexOf(delimiter)+1);
        final String signature = methodHash.substring(methodHash.indexOf(delimiter)+delimiter.length());
        
        Class<?> c = getClassWithHash(classHash);
        
        for(Method m:c.getDeclaredMethods()){
            final String name = m.getName() + Type.getMethodDescriptor(m);
            
            if(name.equals(signature)){
                return m;
            }
        }
        
        throw new RuntimeException(
            "unable to locate method with hash " + methodHash + 
            " in " + c.getName() + " with hash " + classHash
            );
    }
    
    public Collection<Class<?>> loadEverythingPassingFilter(
            AnalysisFilter f
            ) throws ClassNotFoundException{
        List<Class<?>> classes = new ArrayList<>();
        for(String key:bytecodeMap.keySet()){
            if(f.shouldAnalyzeClass(key)){
                classes.add(findClass(key));
            }
        }
        
        return classes;
    }
    
    public Collection<Class<?>> loadEverythingWithPrefix(
            final String classNamePrefix
            ) throws ClassNotFoundException{
        
        List<Class<?>> classes = new ArrayList<>();
        for(String key:bytecodeMap.keySet()){
            if(key.startsWith(classNamePrefix)){
                logger.warn("loading " + key);
                classes.add(findClass(key));
            }
        }
        
        return classes;
    }
    
    public void registerClasspathDir(File dir) throws IOException{
        Collection<File> files = FileUtils.listFiles(
            dir, 
            new String[]{"class", "jar", "aar"},
            true
            );
        
        for(File f:files){
            byte[] bytes = FileUtils.readFileToByteArray(f);
            if(f.getName().endsWith(".jar") || f.getName().endsWith(".aar")){
                registerJar(bytes);
            } else if(f.getName().endsWith(".class")){
                registerClass(bytes);
            }
        }
    }
    
    public void registerJar(byte[] jar) throws IOException{
        
        JarTraverser.traverseJar(
            jar,
            (hash,bytecode)->registerClass(bytecode)
            );
    }
    
    public void registerClass(final byte[] classBytecode){
        ClassNode cn = BytecodeHelper.getClassNode(classBytecode);
        
        final String name = cn.name.replace("/", ".");
        
        if(name.startsWith("mil.darpa.immortals.annotation.dsl")){
            logger.warn(
                "ignoring IMMoRTALS annotation class on app classpath, " +
                "instead it will be loaded from the analyzer's classpath: " + name
                );
            return;
        }
        
        logger.info("registered " + name);
        
        bytecodeMap.put(name, classBytecode);
        
        final String hash = ImmortalsPointerHelper.hash(classBytecode);
        
        classNameToClassHashMap.put(name, hash);
    }
    
    public boolean isKnownClass(String s){
        return bytecodeMap.containsKey(s);
    }
    
    public byte[] getBytecode(Class<?> c) throws IOException{
        
        final String className = c.getName();
        
        if(bytecodeMap.containsKey(className)){
            return bytecodeMap.get(className);
        }
        
        final String resourceName = className.replace(".", "/") + ".class";
        
        InputStream s = parent.getResourceAsStream(resourceName);
        
        if(s == null){
            throw new RuntimeException("couldn't find " + resourceName);
        }
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        IOUtils.copy(s, os);
        
        return os.toByteArray();
    }
    
    /**
     * Applies a transformer to all classes on the analysis classpath,
     * regardless of whether those classes have been previously loaded
     * 
     * @param transformer
     */
    public void applyTransformer(
            IBytecodeTransformerClass transformer
            ){
        
        logger.trace(String.format("applying transformer " + transformer.getClass().getName()));
        
        for(final String className:bytecodeMap.keySet()){
            transform(className,transformer);
        }
    }
    
    /**
     * 
     * @return the lazy transformation chain
     */
    public List<IBytecodeTransformerClass> getLazyTransformerChain(){
        return lazyTransformers;
    }
    
    private boolean isCurrentlyDefined(final String className){
        if(permgen.containsKey(className)){
            return true;
        }
        
        return false;
    }
    
    private void transform(
            final String className,
            IBytecodeTransformerClass transformer
            ){
        final boolean isAlreadyDefined = isCurrentlyDefined(className);
        if(isAlreadyDefined){
            //TODO: here we skip any class that has already been loaded to
            //      prevent duplicate class issues.  But maybe that's OK?
            
            logger.warn(String.format(
                "instrumenting class %s, which was already defined " +
                "(this may cause unusual behavior)", 
                className
                ));
        }
        
        final byte[] bytecode = bytecodeMap.get(className);
        
        final String classHash = classNameToClassHashMap.get(className);
        
        final boolean shouldFilter = transformer.shouldFilterByName(
            classHash,
            className
            );
        
        if(shouldFilter){
            logger.trace(String.format("skipping %s\n", className));
            return;
        }
        
        boolean wasTransformed = false;
        
        ClassNode cn = BytecodeHelper.getClassNode(bytecode);
        
        if(!transformer.isSafeToTransform(cn)){
            logger.warn(
                String.format(
                    "ignoring class %s because it contains unsafe " +
                    "instructions", 
                    className
                    )
                );
            return;
        }
        
        wasTransformed |= transformer.transformClass(
            classHash, 
            cn
            );
        
        IBytecodeTransformerMethod methodTransformer = 
                transformer.acquireMethodTransformer(
                    classHash, 
                    cn
                    );
        
        List<MethodNode> methods = new ArrayList<>();
        if(cn.methods != null){
            methods.addAll(cn.methods);
        }
        for(MethodNode mn:methods){
            final String methodHash = 
                    ImmortalsPointerHelper.pointerForMethod(
                        classHash, 
                        mn.name, 
                        mn.desc
                        );
            
            boolean shouldTransformMethod = 
                    methodTransformer.shouldTransform(
                        classHash, 
                        methodHash, 
                        className, 
                        mn.name,
                        mn.desc
                        );
            
            if(!shouldTransformMethod){
                continue;
            }
            
            if(!methodTransformer.isSafeToTransform(mn)){
                continue;
            }
            
            wasTransformed |= methodTransformer.transformMethod(
                classHash, 
                methodHash, 
                cn, 
                mn
                );
        }
        
        if(!wasTransformed){
            return;
        }
        
        final byte[] resultantBytecode = getClassBytes(cn);
        
        bytecodeMap.put(className, resultantBytecode);
        
        if(isAlreadyDefined){
            logger.warn(
                String.format(
                    "about to redefine class %s, which may result in unusual " +
                    "behavior", className
                    )
                );
            
            Class<?> c = this.defineClass(
                className, 
                resultantBytecode, 
                0, 
                resultantBytecode.length
                );
            
            permgen.put(className, c);
        }
    }
    
    private byte[] getClassBytes(ClassNode cn) {
        int flags = 0x0;
        if (cn.version > Opcodes.V1_5) {
            // if it's a Java 6 or newer class, frames are mandatory
            // with this flag set, COMPUTE_MAXS happens by default
            flags |= ClassWriter.COMPUTE_FRAMES;
        } else {
            // if it's a Java 5 or newer class, frames are optional but we still
            // need
            // to know the max locals and max stack values
            flags |= ClassWriter.COMPUTE_MAXS;
        }

        ClassWriter cw = getClassWriter(this,flags);

        cn.accept(cw);

        return cw.toByteArray();
    }
    
    private static ClassWriter getClassWriter(
            final AnalysisClassloader classloader,
            final int flags
            ) {
        return new ClassWriter(flags) {

            @Override
            protected String getCommonSuperClass(
                    final String type1,
                    final String type2
                    ) {
                //taken from ASM
                Class<?> c, d;
                try {
                    c = Class.forName(type1.replace('/', '.'), false, classloader);
                    d = Class.forName(type2.replace('/', '.'), false, classloader);
                } catch (Exception e) {
                    throw new RuntimeException(e.toString());
                }
                if (c.isAssignableFrom(d)) {
                    return type1;
                }
                if (d.isAssignableFrom(c)) {
                    return type2;
                }
                if (c.isInterface() || d.isInterface()) {
                    return "java/lang/Object";
                } else {
                    do {
                        c = c.getSuperclass();
                    } while (!c.isAssignableFrom(d));
                    return c.getName().replace('.', '/');
                }
            }
        };
    }
    
    private byte[] getClassBytecode(final String name){
        //first apply all lazy transformers
        for(IBytecodeTransformerClass transformer:lazyTransformers){
            transform(name,transformer);
        }
        
        //now return the possibly modified bytecode from the map
        return bytecodeMap.get(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return findClass(name);
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        
        if(permgen.containsKey(name)){
            logger.trace(String.format("loading %s from permgen\n", name));
            
            return permgen.get(name);
        }
        
        if(bytecodeMap.containsKey(name)){
            logger.trace(String.format("loading %s from analysis classpath\n", name));
            
            final byte[] bytecode = getClassBytecode(name);
            Class<?> c = this.defineClass(name, bytecode, 0, bytecode.length);
            
            permgen.put(name, c);
            
            return c;
        }
        
        logger.trace(String.format("loading %s from parent\n", name));
        
        return parent.loadClass(name);
    }
    
    
    
    
    
    

}
