package com.securboration.immortals.instantiation.bytecode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class SourceFinder {
    
    private static final Logger logger = 
            LogManager.getLogger(SourceFinder.class);

    private final List<String> sourceBases = new ArrayList<>();
    
    private final Map<String,SourceInfo> classNamesToFiles = new HashMap<>();
    
    public static class SourceInfo{
        private final String className;//e.g., java.lang.String
        private final String fileSystemPath;
        private final String repoUrl;
        public SourceInfo(String className, String fileSystemPath,
                String repoUrl) {
            super();
            this.className = className;
            this.fileSystemPath = fileSystemPath;
            this.repoUrl = repoUrl;
            
            System.out.printf(
                    "\tdiscovered class\n\t\t%s\n\t\t@%s\n\t\t@@%s\n", 
                    className, 
                    fileSystemPath,
                    repoUrl);
        }
        public String getClassName() {
            return className;
        }
        public String getFileSystemPath() {
            return fileSystemPath;
        }
        public String getRepoUrl() {
            return repoUrl;
        }
    }
    
    private static String getPath(String path) throws IOException{
        return new File(path).toPath().toRealPath().toFile().getAbsolutePath().replace("\\", "/");
    }
    
    public SourceFinder(
            String projectRoot,
            String repositoryUrl,
            String...sourceRoots
            ) throws IOException{
        sourceBases.addAll(Arrays.asList(sourceRoots));
        
        final String projectRootPath = 
                getPath(projectRoot);
        
        for(String sourceRoot:sourceRoots){
            if(!new File(sourceRoot).exists()){
                System.err.println(
                    "provided src dir does not exist: " + sourceRoot
                    );
                continue;
            }
            
            final String rootFilePath = getPath(sourceRoot);
            
            for(File f:FileUtils.listFiles(new File(sourceRoot), new String[]{"java"}, true)){
                
                final String path = getPath(f.getAbsolutePath());
                
                final String pathWithoutJavaSuffix = path.substring(0,path.lastIndexOf(".java"));
                
                final String classNameInternal = pathWithoutJavaSuffix.replace(rootFilePath+"/", "");
                
                final String className = classNameInternal.replace("/", ".");
                
                final String repoPath = repositoryUrl+"/"+path.replace(projectRootPath+"/","");
                
                if(classNamesToFiles.containsKey(className)){
                    logger.warn("found " + className + " at location [" + path + "] and at location [" + classNamesToFiles.get(className).fileSystemPath + "]");
                }
                
                classNamesToFiles.put(
                        className, 
                        new SourceInfo(className,path,repoPath));
            }
        }
        
    }

    /**
     * 
     * @param classInternalName
     *            e.g., java/lang/String
     * @return the info about the source corresponding to that class name, if it
     *         exists
     */
    public SourceInfo getSourceInfo(String classInternalName){
        
        String className = classInternalName.replace("/", ".");
        
        if(className.contains("$")){
            //internal classes are defined in their parent classfile
            className = className.substring(0, className.indexOf("$"));
        }
        
        return classNamesToFiles.get(className);
    }
    
}
