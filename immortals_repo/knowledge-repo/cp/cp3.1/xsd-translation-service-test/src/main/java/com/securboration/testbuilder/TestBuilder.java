package com.securboration.testbuilder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public class TestBuilder {
    
    public static void main(String[] args) throws Exception {
        final File mdlDir = new File("C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\zDelete\\mdl\\");
        final File outputDirRaw = new File("./output/mdl-raw");
        final File outputDirPruned = new File("./output/mdl-clean");
        final File outputDirTranslationProblems = new File("./output/mdl-translation-problems");
        
        {//purge output dir
            if(outputDirRaw.exists()){
                FileUtils.deleteDirectory(outputDirRaw);
            }
            FileUtils.forceMkdir(outputDirRaw);
        }
        
        System.out.printf("using %s\n", mdlDir.getAbsolutePath());
        for(File zip:FileUtils.listFiles(mdlDir, new String[]{"zip"}, true)){
            if(!zip.getName().contains("_with_Examples")){
                continue;
            }//TODO: skip anything that doesn't have XML examples
            
            System.out.printf("\t%s\n", zip.getName());
            
            process(zip,outputDirRaw);
        }
        
        MdlIntegrityChecker.retainGoodMdlVersions(
            outputDirRaw, 
            outputDirPruned,
            outputDirTranslationProblems
            );
    }
    
    private static void process(
            final File mdlZip,
            final File outputDir
            ) throws FileNotFoundException, IOException{
        
        final String base = FilenameUtils.removeExtension(mdlZip.getName()).replace("_with_Examples","");
        
        {//exemplar XMLs
            copy(
                getEntries(mdlZip,".xml","example-stage"),
                new File(new File(outputDir,base),"datasource")
                );
        }
        
        {//schema
            copy(
                getEntries(mdlZip,".xsd"),
                new File(new File(outputDir,base),"schema")
                );
        }
        
        if(false){//xsl
            copy(
                getEntries(mdlZip,".xsl"),
                new File(new File(outputDir,base),"xsl")
                );
        }
    }
    
    private static void copy(
            Map<String,byte[]> data, 
            final File intoDir
            ) throws IOException{
        for(String key:data.keySet()){
            final File keyFile = new File(key);
            
            FileUtils.writeByteArrayToFile(
                new File(intoDir,keyFile.getName()), 
                data.get(key)
                );
        }
    }
    
    
    private static Map<String,byte[]> getEntries(
            final File zipArchive, 
            final String suffix,
            final String...excludes
            ) throws FileNotFoundException, IOException{
        
        final Map<String,byte[]> map = new LinkedHashMap<>();
        try(ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipArchive)))){
            while(true){
                final ZipEntry entry = zis.getNextEntry();
                
                if(entry == null){
                    break;
                }
                
                if(entry.getName() != null && entry.getName().endsWith(suffix)){
                    boolean excluded = false;
                    {
                        for(String exclude:excludes){
                            if(entry.getName().startsWith(exclude)){
                                excluded = true;
                            }
                        }
                    }
                    if(excluded){
                        continue;
                    }
                    
                    final byte[] data = IOUtils.toByteArray(zis);
                    map.put(entry.getName(), data);
                    
                    System.out.printf("\t\t%s (%dB)\n", entry.getName(), data.length);
                }
            }
        }
        
        return map;
    }

}
