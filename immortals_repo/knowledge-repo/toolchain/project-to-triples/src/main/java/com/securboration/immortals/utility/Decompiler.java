package com.securboration.immortals.utility;

import org.apache.commons.io.FileUtils;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Decompiler {
    
    private Map<String, Object> options = new HashMap<>();
    
    public Decompiler() {
        options.put(IFernflowerPreferences.LOG_LEVEL, "warn");
        options.put(IFernflowerPreferences.REMOVE_SYNTHETIC, "1");
        options.put(IFernflowerPreferences.LITERALS_AS_IS, "1"); 
        options.put(IFernflowerPreferences.RENAME_ENTITIES, "1");
        options.put(IFernflowerPreferences.SYNTHETIC_NOT_SET, "1");
        options.put(IFernflowerPreferences.ASCII_STRING_CHARACTERS, "1");
    }
    
    public File decompileClassFile(String classFilePath, String javaSourcePath, boolean plugin) throws IOException {
        
        File classFile = new File(classFilePath);
        File sourceFile = new File(javaSourcePath);
        
        ConsoleDecompiler decompiler = new ConsoleDecompiler(classFile.getParentFile(), options);
        decompiler.addSpace(classFile, true);
        decompiler.decompileContext();
        
        if (plugin) {
            classFile = new File(classFile.getAbsolutePath().substring(0, classFile.getAbsolutePath().length() - 5) + "java");
            FileUtils.copyFile(classFile, sourceFile);
        }
        
        return sourceFile;
    }
}
