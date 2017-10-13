package com.securboration.immortals.j2t.analysis;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.securboration.immortals.annotations.helper.AnnotationHelper;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;

/**
 * Converts java classes to a semantic model
 * 
 * @author jstaples
 *
 */
public class ClasspathTraverser {
    private final JavaToTriplesConfiguration context;

    public static interface ClasspathVisitor{
        
        public default void visitBegin(){};
        
        public default void visitClass(Class<?> c){};
        public default void visitSkippedClass(Class<?> c){};
        
        public default void visitEnd(){};
    }
    
    public ClasspathTraverser(JavaToTriplesConfiguration pluginContext) {
        this.context = pluginContext;
    }
    
    private boolean shouldAnalyze(Class<?> c){
        final String className = c.getName();
        
        if(containsIgnore(c)){
            return false;
        }
        
        if(containsConceptInstance(c)){
            return false;
        }
        
        for(String prefix:context.getSkipPrefixes()){
            if(className.startsWith(prefix)){
                return false;
            }
        }
        
        return true;
    }
    
    private boolean containsConceptInstance(Class<?> c){
        
        if(AnnotationHelper.containsAnnotation(c,ConceptInstance.class)){
            return true;
        }
        
        return false;
    }
    
    private boolean containsIgnore(Class<?> c){
        
        if(AnnotationHelper.containsAnnotation(c,Ignore.class)){
            return true;
        }
        
        return false;
    }
    
    public void traverse(ClasspathVisitor v) throws ClassNotFoundException {

        v.visitBegin();
        
        for(String classPath:context.getClassPaths()){
            final File targetDir = new File(classPath);
            
            if(!targetDir.isDirectory()){
                throw new RuntimeException(classPath + " is not a directory");
            }
            
            final String classpathPrefix = 
                    targetDir.getAbsolutePath().replace("\\", "/")+"/";
            
            for (File classFile : 
                FileUtils.listFiles(
                        targetDir,
                        new String[] { "class" }, 
                        true)) {
                
                final String path = 
                        classFile.getAbsolutePath().replace("\\", "/");
                String className = 
                        path.replace(classpathPrefix, "").replace(".class", "");
    
                className = className.replace("/", ".");
                
                Class<?> c = context.getClassloader().loadClass(className);
                
                if(!shouldAnalyze(c)){
                    
                    context.getLog().info(
                            "Skipping " + className + 
                            " because of a skip prefix match "
                            + "or @Ignore annotation");
                    
                    v.visitSkippedClass(c);
                    continue;
                }
                
                v.visitClass(c);
            }
        }
        
        v.visitEnd();
    }

}
