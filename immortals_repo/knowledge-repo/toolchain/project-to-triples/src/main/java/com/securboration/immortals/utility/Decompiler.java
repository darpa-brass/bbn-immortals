package com.securboration.immortals.utility;

import com.strobel.decompiler.PlainTextOutput;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Decompiler {
    
    public File decompileClassFile(String classFilePath, String javaSourcePath, boolean plugin) throws IOException {
        
        //File classFile = new File(classFilePath);
        File sourceFile = new File(javaSourcePath);
        
        try (final FileOutputStream fileOutputStream = new FileOutputStream(sourceFile);
             final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream)) {
            
            com.strobel.decompiler.Decompiler.decompile(classFilePath, 
                    new PlainTextOutput(outputStreamWriter));
            
        } catch (IOException exc) {
            exc.printStackTrace();
        }
        
        //TODO -> I forget why I did this, look into soon...
        //if (!plugin) {
            //classFile = new File(classFile.getAbsolutePath().substring(0, classFile.getAbsolutePath().length() - 5) + "java");
           // FileUtils.copyFile(classFile, sourceFile);
       // }
        
        return sourceFile;
    }
}
