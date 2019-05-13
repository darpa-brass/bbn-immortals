package com.securboration.immortals.bcd;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;


public class ClasspathTraverser {
    
    public static void traverse(
            final Collection<File> classpathRoots,
            final IClasspathVisitor visitor
            ) throws IOException {
        for(File f:classpathRoots){
            traverse(f,visitor);
        }
    }
    
    public static void traverse(
            final File classpathRoot,
            final IClasspathVisitor visitor
            ) throws IOException {
        if(classpathRoot.isFile()){
            //it's a naked CPE
            final byte[] data = FileUtils.readFileToByteArray(classpathRoot);
            
            visitor.visitClasspathElement(
                classpathRoot.getName(), 
                data
                );
            
            if(classpathRoot.getName().endsWith(".jar")){
                traverse(data,visitor);
            }
        } 
        
        if(!classpathRoot.isDirectory()){
            //it's not a file and not a directory, so halt
            return;
        }
        
        //it's a directory containing zero or more CPEs
        
        final String rootPath = classpathRoot.getCanonicalPath();
        for(File classpathElement:FileUtils.listFiles(classpathRoot, null, true)){
            //root: proj/test/build
            //elem: proj/test/build/com/securboration/test/apkg/AClass.class
            //path:                 com/securboration/test/apkg/AClass.class
            final String elementPath = classpathElement.getCanonicalPath();
            final String classpathName = elementPath.substring(rootPath.length()+1);
            
            final byte[] data = FileUtils.readFileToByteArray(classpathElement);
            
            visitor.visitClasspathElement(classpathName, data);
            
            if(classpathElement.getName().endsWith(".jar")){
                traverse(data,visitor);
            }
        }
    }

    public static void traverse(
            final byte[] jar, 
            final IClasspathVisitor visitor
            ) throws IOException {
        visitor.beforeTraversal();
        traverseJar(new ByteArrayInputStream(jar), visitor, 0);
        visitor.afterTraversal();
    }

    private static Iterator<? extends ZipEntry> acquireIterator(
            Enumeration<? extends ZipEntry> entries) {
        return new Iterator<ZipEntry>() {

            @Override
            public boolean hasNext() {
                return entries.hasMoreElements();
            }

            @Override
            public ZipEntry next() {
                return entries.nextElement();
            }

        };
    }

    private static File copyToTempFile(InputStream jarBytes)
            throws IOException {
        File f = File.createTempFile("rampart", ".jar");
        f.deleteOnExit();
        FileUtils.writeByteArrayToFile(f, IOUtils.toByteArray(jarBytes));

        return f;
    }

    private static void traverseJar(
            final InputStream inputJar, 
            final IClasspathVisitor transformer, 
            final int depth
            ) throws IOException {
        if(depth > 10){
            throw new RuntimeException("current depth is " + depth + ", which seems to indicate infinite recursion.  Aborting traversal.");
        }
        
        // create a temp file containing the JAR
        final File jarTempFile = copyToTempFile(inputJar);

        try (final ZipFile inJar = new ZipFile(jarTempFile);) {

            final Iterator<? extends ZipEntry> iterator =
                acquireIterator(inJar.entries());

            // extract everything from the jar
            while (iterator.hasNext()) {
                final ZipEntry jarEntry = iterator.next();

                final byte[] jarData =
                    IOUtils.toByteArray(inJar.getInputStream(jarEntry));
                
                if(!jarEntry.isDirectory()){
                    transformer.visitClasspathElement(
                        jarEntry.getName(),
                        jarData
                        );
                }

                if (jarEntry.getName().endsWith(".jar")) {
                    // log("found a nested jar: " + jarEntry.getName());

                    // it's a nested jar, recursively visit its contents
                    traverseJar(
                        new ByteArrayInputStream(jarData),
                        transformer,
                        depth + 1);
                }
            }
        }
    }

}
