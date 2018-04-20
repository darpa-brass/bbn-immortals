package com.securboration.immortals.instantiation.annotationparser.traversal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.securboration.immortals.instantiation.annotationparser.bytecode.BytecodeHelper;
import com.securboration.immortals.instantiation.annotationparser.bytecode.Console;

/**
 * Ingests a binary bytecode artifact
 * 
 * @author jstaples
 *
 */
public class JarTraverser {
    
    private JarTraverser(){}
    
    public static void traverseNakedClasspath(
            File dirContainingClasses,
            BytecodeArtifactVisitor visitor
            ) throws IOException {
        
        JarTraverser traverser = new JarTraverser();

        traverser.openNakedClasspathDir(
            dirContainingClasses, 
            visitor
            );
    }
    
    public static void traverseJar(
            File jar,
            BytecodeArtifactVisitor visitor
            ) throws IOException{
        JarTraverser ingestor = new JarTraverser();
        
        ingestor.openJar(
                new ByteArrayInputStream(FileUtils.readFileToByteArray(jar)),
                visitor
                );
    }
    
    public static void traverseJar(
            byte[] jar,
            BytecodeArtifactVisitor visitor
            ) throws IOException{
        JarTraverser ingestor = new JarTraverser();
        
        ingestor.openJar(
                new ByteArrayInputStream(jar),
                visitor
                );
    }
    
    private void openNakedClasspathDir(
            File classpathRoot,
            BytecodeArtifactVisitor visitor
            ) throws IOException{
        Console.log("indexing classpath...", classpathRoot);
        
        Collection<File> classes = 
                FileUtils.listFiles(
                    classpathRoot, 
                    new String[]{"class"}, 
                    true
                    );
        
        for(File f:classes){
            final byte[] bytecode = 
                    FileUtils.readFileToByteArray(f);
            
            final String hash = 
                    BytecodeHelper.hash(bytecode);
            
            visitor.visitClass(
                hash, 
                bytecode
                );
        }
    }

    private void openJar(
            InputStream jarWithDependenciesPath,
            BytecodeArtifactVisitor visitor
            ) throws IOException{
        Console.log("indexing jar...", jarWithDependenciesPath);
        
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
                            Console.log("found a nested jar: " + jarEntry.getName());
                            
                            //it's a nested jar, recurse
                            openJar(
                                    new ByteArrayInputStream(jarContent),
                                    visitor
                                    );
                        } else if (jarEntry.getName().endsWith(".class")) {
                            
                            final byte[] bytecode = 
                                    jarContent;
                            
                            final String hash = 
                                    BytecodeHelper.hash(bytecode);
                            
                            visitor.visitClass(hash, bytecode);
                        }
                    }
                }
            }
        }
    }

}
