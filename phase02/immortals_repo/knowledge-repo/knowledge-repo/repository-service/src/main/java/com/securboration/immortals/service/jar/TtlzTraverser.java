package com.securboration.immortals.service.jar;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Ingests a ttlz archive.  A ttlz archive is simply a JAR (which is a ZIP) 
 * containing various .ttl files
 * 
 * @author jstaples
 *
 */
public class TtlzTraverser {
    
    private static final Set<String> ARCHIVE_SUFFIXES = 
            new HashSet<>(Arrays.asList(".jar",".ttlz",".zip"));
    
    private TtlzTraverser(){}
    
    /**
     * Ingest a ttlz archive as a stream of bytes
     * 
     * @param ttlz
     *            a stream of bytes
     * @param visitor
     *            the content visitor to receive notifications
     * @throws IOException
     *             if something goes awry
     */
    public static void traverse(
            InputStream ttlz,
            TtlzContentVisitor visitor
            ) throws IOException{
        TtlzTraverser.openTtlz(
                ttlz,
                n->n.endsWith(".ttl"),
                visitor
                );
    }
    
    /**
     * Ingest an exploded ttlz archive from a file or directory
     * 
     * @param fileOrDir
     *            a file that is a ttlz archive OR a directory containing the
     *            contents of an exploded ttlz archive
     * @param visitor
     *            the content visitor
     * @throws IOException
     *             if something goes awry
     */
    public static void traverse(
            File fileOrDir,
            TtlzContentVisitor visitor
            ) throws IOException {
        TtlzTraverser.openNakedClasspathDir(
            fileOrDir,
            n->n.endsWith(".ttl"),
            visitor
            );
    }
    
    private static void traverse(
            byte[] ttlz,
            TtlzContentFilter filter,
            TtlzContentVisitor visitor
            ) throws IOException{
        TtlzTraverser.openTtlz(
                new ByteArrayInputStream(ttlz),
                filter,
                visitor
                );
    }
    
    private static void openNakedClasspathDir(
            File fileOrDir,
            TtlzContentFilter filter,
            TtlzContentVisitor visitor
            ) throws IOException{
        Collection<File> files = new ArrayList<>();
        
        if(fileOrDir.isDirectory()){
            files.addAll(
                FileUtils.listFiles(
                    fileOrDir, 
                    null, 
                    true
                    )
                );
        } else if(fileOrDir.isFile()){
            files.add(fileOrDir);
        }
        
        for(File f:files){
            final byte[] binaryData = 
                    FileUtils.readFileToByteArray(f);
            
            if(isZipArchive(f.getName())){
                //it's an archive so traverse it
                traverse(binaryData,filter,visitor);
            } else {
                if(filter.shouldVisit(f.getAbsolutePath())){
                    visitor.visit(
                        f.getAbsolutePath(), 
                        binaryData
                        );
                }
            }
        }
    }
    
    private static boolean isZipArchive(String name){
        name = name.toLowerCase();
        for(String suffix:ARCHIVE_SUFFIXES){
            if(name.endsWith(suffix)){
                return true;
            }
        }
        
        return false;
    }

    private static void openTtlz(
            InputStream currentTtlz,
            TtlzContentFilter filter,
            TtlzContentVisitor visitor
            ) throws IOException{
        try(JarArchiveInputStream inJar = new JarArchiveInputStream(currentTtlz);)
        {
            final int MAX_SIZE = 
                    1024*1024//1MB
                    *256;
            
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
                            "entry too large, > " + MAX_SIZE + " bytes"
                            );
                    } else if (jarEntry.getSize() == 0) {
                        // do nothing, the entry is not a file
                    } else {
                        final int length = IOUtils.read(inJar, buffer);
                        byte[] jarContent = new byte[length];
    
                        System.arraycopy(buffer, 0, jarContent, 0, length);
    
                        if(isZipArchive(jarEntry.getName())) {
                            //it's a nested jar, recurse
                            openTtlz(
                                    new ByteArrayInputStream(jarContent),
                                    filter,
                                    visitor
                                    );
                        } else if (filter.shouldVisit(jarEntry.getName())) {
                            
                            final byte[] binaryContent = 
                                    jarContent;
                            
                            visitor.visit(
                                jarEntry.getName(), 
                                binaryContent
                                );
                        }
                    }
                }
            }
        }
    }

}
