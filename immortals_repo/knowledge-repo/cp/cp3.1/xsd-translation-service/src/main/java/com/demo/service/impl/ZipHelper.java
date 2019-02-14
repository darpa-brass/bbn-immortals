package com.demo.service.impl;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;

public class ZipHelper{
    
    public interface ZipArchiveTransformer {
        public boolean shouldExclude(ZipEntry e);
        
        public Map<String,byte[]> getNewData();
    }
    
    public static void addEntries(
            ZipOutputStream zos, 
            File rootOfArchive
            ) throws IOException{
        final String rootPath = rootOfArchive.getAbsolutePath();
        
        for(File f:FileUtils.listFiles(rootOfArchive, null, true)){
            final String entryPath = f.getAbsolutePath();
            final String common = StringUtils.getCommonPrefix(rootPath,entryPath);
            final String trimmedPath = entryPath.substring(common.length());
            
            final String pathToUse = "ess" + trimmedPath;
            
            addEntry(zos,pathToUse,FileUtils.readFileToByteArray(f));
        }
    }
    
    public static void addEntry(
            ZipOutputStream zos, 
            String name, 
            byte[] data
            ) throws IOException{
        ZipEntry ze = new ZipEntry(name);
        zos.putNextEntry(ze);
        zos.write(data);
        zos.closeEntry();
    }
    
    public static byte[] zip(File zipThis) throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(out));
        
        addEntries(zout,zipThis);
        
        zout.close();
        
        return out.toByteArray();
    }
    
    public static void addEntry(
            ZipOutputStream zos, 
            String name, 
            String pathToFile
            ) throws IOException{
        File f = new File(pathToFile);
        
        if(!f.exists()){
            addEntry(zos,name,String.format("<empty> because %s does not exist\n", pathToFile));
        } else {
            addEntry(zos,name,FileUtils.readFileToByteArray(f));
        }
    }
    
    
    public static byte[] transform(
            final byte[] zipArchive,
            final ZipArchiveTransformer...transformers
            ) throws IOException{
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        try(ZipOutputStream zos = new ZipOutputStream(out)){
            try(ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipArchive))){
                
                ZipEntry zipEntry = zis.getNextEntry();
                
                while (zipEntry != null) {
                    boolean shouldExclude = false;
                    
                    {//determine whether the entry should be filtered
                        for(ZipArchiveTransformer t:transformers){
                            if(t.shouldExclude(zipEntry)){
                                shouldExclude = true;
                                break;
                            }
                        }
                    }
                    
                    if(!shouldExclude){
                        final ZipEntry newEntry = new ZipEntry(zipEntry.getName());
                        zos.putNextEntry(newEntry);
                        
                        IOUtils.copy(zis, zos);
                        
                        zos.closeEntry();
                    }
                    
                    zipEntry = zis.getNextEntry();
                }
                
                zis.closeEntry();
                
                for(final ZipArchiveTransformer transformer:transformers){
                    if(transformer.getNewData() == null){
                        continue;
                    }
                    
                    final Map<String,byte[]> newData = transformer.getNewData();
                    for(final String key:newData.keySet()){
                        final byte[] data = newData.get(key);
                        
                        if(data == null){
                            continue;
                        }
                        
                        ZipEntry newEntry = new ZipEntry(key.replace("\\","/"));
                        zos.putNextEntry(newEntry);
                        zos.write(data);
                        zos.closeEntry();
                    }
                }
            }
        }
        
        return out.toByteArray();
    }
    
    public static void unzip(
            final byte[] archiveToUnzip,
            final File unzipIntoThisDir,
            final ZipArchiveTransformer...transformers
            ) throws IOException{
        
        try(ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(archiveToUnzip))){
            final byte[] buffer = new byte[4096];
            ZipEntry zipEntry = zis.getNextEntry();
            
            while (zipEntry != null) {
                {//determine whether the entry should be filtered
                    boolean shouldExclude = false;
                    
                    for(ZipArchiveTransformer t:transformers){
                        if(t.shouldExclude(zipEntry)){
                            shouldExclude = true;
                            break;
                        }
                    }
                    
                    if(shouldExclude){
                        continue;
                    }
                }
                
                {
                    File newFile = new File(unzipIntoThisDir, zipEntry.getName());
                    ByteArrayOutputStream data = new ByteArrayOutputStream();
                    
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        data.write(buffer, 0, len);
                    }
                    
                    FileUtils.writeByteArrayToFile(newFile,data.toByteArray());
                    

//                    System.out.printf(
//                        "\t%dB written to %s\n",
//                        data.size(),
//                        newFile.getAbsolutePath()
//                        );
                }
                
                zipEntry = zis.getNextEntry();
            }
            
            zis.closeEntry();
            
            for(final ZipArchiveTransformer transformer:transformers){
                if(transformer.getNewData() == null){
                    continue;
                }
                
                final Map<String,byte[]> newData = transformer.getNewData();
                for(final String key:newData.keySet()){
                    final byte[] data = newData.get(key);
                    
                    if(data == null){
                        continue;
                    }
                    
                    final File outputHere = new File(unzipIntoThisDir,key);
                    FileUtils.writeByteArrayToFile(outputHere, data);
                }
            }
        }
    }
}
