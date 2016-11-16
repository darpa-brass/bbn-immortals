package com.securboration.immortals.tools.scanner.main;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;


public class JarIngestor {
    
    private final Report report = new Report();
    
    private static final boolean DUMP_TO_STDOUT = 
            System.getProperty("dump_stdout") != null;
    
    private static final boolean DEEP_DIVE = 
            System.getProperty("deep_dive") != null;
    
    public static void main(String[] args) throws IOException{
        
//        args = new String[]{
//                "C:/Users/Securboration/Desktop/code/immortals/trunkDelete2/build",
//                "C:/Users/Securboration/Desktop/code/immortals/trunkDelete2/client",
//                "C:/Users/Securboration/Desktop/code/immortals/trunkDelete2/shared",
//                
////                "C:/Users/Securboration/Desktop/code/stampedeWorking/services/pps-server/target/metatagger-pps-boot-rsmt.war"
//                
////                "C:/Users/Securboration/Desktop/code/immortals/trunkDelete2/client/ATAKLite"
//        };
        
        
        if(args.length == 0){
            System.out.println(
                "How to use this tool:\n" +
                "  Accepts 1 or more command line args.\n" +
                "  Each arg is a path.\n" +
                "  Each path points to a jar, a class, or a directory containing jars and classes."
                );
            System.exit(-1);
        }
        
        final long start = System.currentTimeMillis();
        
        JarIngestor j = new JarIngestor();
        
        j.report.log("[begin]\n");
        
        for(String s:args){
            File f = new File(s);
            
            if(!f.exists()){
                throw new RuntimeException(
                    "arg value " + s + " does not point to a valid file"
                    );
            }
            
            j.openNakedClasspathDir(f);
        }
        
        FileUtils.writeStringToFile(
            new File("report.dat"),
            j.report.toString()
            );
        
        if(DUMP_TO_STDOUT){
            System.out.println(j.report.toString());
        }
        
        final long end = System.currentTimeMillis();
        
        System.out.printf("done in %dms\n", end - start);
    }
    
    
    
    private static ClassNode getClassNode(byte[] bytecode){
        ClassReader cr = new ClassReader(bytecode);
        ClassNode cn = new ClassNode();

        cr.accept(cn, 0);// 0 = Don't expand frames or compute stack/local
                         // mappings
        
        return cn;
    }
    
    private void openNakedClasspathDir(
            File classpathRoot
            ) throws IOException{
        
//        log("traversing classpath root @ %s\n",classpathRoot.getPath());

        Collection<File> filesToTraverse = 
                classpathRoot.isDirectory() ? 
                        FileUtils.listFiles(classpathRoot, null, true) 
                        : 
                        Arrays.asList(classpathRoot);
                       
        for(File f:filesToTraverse){
            final String name = f.getAbsolutePath();
            final byte[] binary = FileUtils.readFileToByteArray(f);
            
//            log("encountered classpath item @ %s\n",name);
            
            if(f.getName().endsWith(".class")){
                //it's a class, process it
                processClass(
                    name,
                    hash(binary),
                    getClassNode(binary)
                    );
            } else if(isJar(f.getName())){
                //it's a jar, process it
                openJar(
                    name,
                    new ByteArrayInputStream(binary)
                    );
            } else {
                //it's a resource, process it
                processResource(
                    f.getPath(),
                    hash(binary),
                    binary
                    );
            }
        }
    }
    
    private static boolean isJar(final String name){
        if(name.endsWith(".jar")){
            return true;
        }
        
        if(name.endsWith(".war")){
            return true;
        }
        
        if(name.endsWith(".aar")){
            return true;
        }
        
        if(name.endsWith(".ear")){
            return true;
        }
        
        return false;
    }

    private void openJar(
            final String jarName,
            InputStream jarWithDependenciesPath
            ) throws IOException{
        
        report.jarCount.incrementAndGet();
        
//        log("traversing jar %s\n",jarName);

        try(JarArchiveInputStream inJar = new JarArchiveInputStream(jarWithDependenciesPath);)
        {
            final int MAX_SIZE = 
                    1024*1024//1MiB
                    *256
                    ;//256MiB
            
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
                        
                        final String entryName = 
                                jarName + "/" + jarEntry.getName();
                        
                        final int length = IOUtils.read(inJar, buffer);
                        byte[] jarContent = new byte[length];
    
                        System.arraycopy(buffer, 0, jarContent, 0, length);
    
                        if(isJar(jarEntry.getName())) {
                            report.log("found a nested jar: " + jarEntry.getName());
                            
                            //it's a nested jar, recurse
                            openJar(
                                jarName + "/" + jarEntry.getName(),
                                new ByteArrayInputStream(jarContent)
                                );
                        } else if (jarEntry.getName().endsWith(".class")) {
                            //it's a class, process it
                            
                            processClass(
                                entryName,
                                hash(jarContent),
                                getClassNode(jarContent)
                                );
                        } else {
                            //it's a resource, do nothing
                            
                            processResource(
                                entryName,
                                hash(jarContent),
                                jarContent
                                );
                        }
                    }
                }
            }
        }
    }
    
    private void deepDive(ClassNode cn){
        
        if(cn.methods == null){
            return;
        }
        
        for(MethodNode mn:cn.methods){
            deepDive(mn);
        }
    }
    
    private void deepDive(MethodNode mn){
        if(mn.instructions == null){
            return;
        }
        
        mn.instructions.iterator().forEachRemaining(i->{
            if(i.getType() == AbstractInsnNode.INVOKE_DYNAMIC_INSN){
                report.values.put("found INVOKE_DYNAMIC", "true");
                
                InvokeDynamicInsnNode id = (InvokeDynamicInsnNode)i;
                
                report.log("\tmethod %s %s issues an INVOKEDYNAMIC on %s", mn.name, mn.desc, id.name);
            }
        });
    }
    
    private void processClass(
            String pathToClass,
            String hash,
            ClassNode cn
            ){
        report.classCount.incrementAndGet();
        report.log("found    class with hash %s version=%3d: %s\n",
            hash,
            cn.version,
            pathToClass
            );
        
        report.classVersions.add(cn.version);
        
        if(DEEP_DIVE){
            deepDive(cn);
        }
    }
    
    private void processResource(
            String pathToResource,
            String hash,
            byte[] resourceContent
            ){
        report.resourceCount.incrementAndGet();
//        report.log("found resource with hash %s: %s\n",hash,pathToResource);
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
        
        private static String getJavaVersion(final int majorVersion){
            
            if(!map.containsKey(majorVersion)){
                return "unknown class version";
            }
            
            return map.get(majorVersion);
            
        }
    }
    
    private static class Report{
        final StringBuilder sb = new StringBuilder();
        
        final Map<String,String> values = new HashMap<>();
        final Set<Integer> classVersions = new HashSet<>();
        final AtomicLong classCount = new AtomicLong();
        final AtomicLong jarCount = new AtomicLong();
        final AtomicLong resourceCount = new AtomicLong();
        final long startTime = System.currentTimeMillis();
        
        
        
        
        @Override
        public String toString(){
            
            sb.append("\nfound the following class versions:\n");
            sb.append("\tsee https://en.wikipedia.org/wiki/Java_class_file\n");
            sb.append("\tsee https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html\n");
            for(int v:new TreeSet<>(classVersions)){
                sb.append("\t\t" + v + "(" + VersionMapper.getJavaVersion(v)+")\n");
            }
            sb.append("\n");
            
            sb.append("discoveries:\n");
            for(String s:new TreeSet<>(values.keySet())){
                sb.append("\t\t" + s + " = " + values.get(s) + "\n");
            }
            sb.append("\n");
            
            final long endTime = System.currentTimeMillis();
            
            log("examined %d jars\n",jarCount.get());
            log("examined %d classes\n",classCount.get());
            log("examined %d resources\n",resourceCount.get());
            
            
            log("done in %dms\n",endTime-startTime);
            
            sb.append("\n\n\n[end]\n");
            
            
            
            return sb.toString();
        }
        
        private void log(String format,Object...args){
            sb.append(String.format(format, args));
            
//            System.out.printf(String.format(format, args));//TODO
        }
    }

}

