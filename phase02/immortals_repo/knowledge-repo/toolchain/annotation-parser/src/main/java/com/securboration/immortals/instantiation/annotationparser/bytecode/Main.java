package com.securboration.immortals.instantiation.annotationparser.bytecode;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.securboration.immortals.instantiation.annotationparser.traversal.JarTraverser;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;

public class Main {
    
    private static String[] getTestArgs(){
        
        return new String[]{
                "r2.0.0",
                "C:/Users/Securboration/Desktop/code/immortals/trunk/shared/IMMORTALS_REPO/mil/darpa",
                "./target/classes/ontology/bytecode-structure",
        };
        
    }
    
    private static class ReflectionHelper{
        public static Field getField(Class<?> c,String fieldName){
            Class<?> currentClass = c;
            
            Field match = null;
            while(currentClass != null){
                for(Field f:currentClass.getDeclaredFields()){
                    if(f.getName().equals(fieldName)){
                        if(match != null){
                            throw new RuntimeException(
                                "found multiple matches for a field named " + 
                                fieldName + " in class " + 
                                currentClass.getName() + 
                                " with base class " + c.getName()
                                );
                        }
                        match = f;
                    }
                }
                
                currentClass = currentClass.getSuperclass();
            }
            
            return match;
        }
    }
    
    private static void addIgnoreFields(ObjectToTriplesConfiguration c){
        c.getIgnoredFields().add(
            ReflectionHelper.getField(
                FieldNode.class, 
                "api"
                )
            );
        c.getIgnoredFields().add(
            ReflectionHelper.getField(
                AnnotationNode.class, 
                "api"
                )
            );
        c.getIgnoredFields().add(
            ReflectionHelper.getField(
                ClassNode.class, 
                "api"
                )
            );
        c.getIgnoredFields().add(
            ReflectionHelper.getField(
                MethodNode.class, 
                "api"
                )
            );
    }
    
    /**
     * args[0]: version
     * args[1]: a dir to recursively traverse containing JARs to process
     * args[2]: an output dir
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

//        args = getTestArgs();//TODO
        
        final String version = args[0];
        final File outputDir = new File(args[1]);
        final List<File> jarDirs = new ArrayList<>();
        {
            final String[] dirs = new String[args.length - 2];
            System.arraycopy(args, 2, dirs, 0, args.length - 2);
            
            for(String dir:dirs){
                jarDirs.add(new File(dir));
            }
        }
        
        for(final File jarDir:jarDirs){
            {//JAR mode
                Console.log("searching %s",jarDir.getAbsolutePath());
                Collection<File> jars = 
                        FileUtils.listFiles(
                            jarDir, 
                            new String[]{"jar"}, 
                            true
                            );
                
                for(File jar:jars){
                    Console.log("found jar %s", jar.getName());
                    
                    ObjectToTriplesConfiguration config = 
                            new ObjectToTriplesConfiguration(version);
                    config.setAddMetadata(false);
                    addIgnoreFields(config);
                    
                    BytecodeModelGatherer gatherer = 
                            new BytecodeModelGatherer();
                    
                    JarTraverser.traverseJar(jar, gatherer);
                    
                    final String outputPath = 
                            outputDir + "/" + jar.getName() + ".bytecode.ttl";
                    
                    Model jarModel = 
                            ObjectToTriples.convert(
                                config, 
                                gatherer.getBytecodeModel()
                                );
                    
                    OntologyHelper.addAutogenerationMetadata(
                        config, 
                        jarModel, 
                        config.getTargetNamespace(), 
                        config.getOutputFile()
                        );
                    
                    final String serialized = 
                            OntologyHelper.serializeModel(
                                jarModel, 
                                "TTL", 
                                config.isValidateOntology()
                                );
                    
//                    Console.log(
//                        "bytecode model for jar %s:\n%s\n", 
//                        jar.getName(), 
//                        serialized
//                        );
                    
                    FileUtils.writeStringToFile(
                        new File(outputPath), 
                        serialized
                        );
                }
            }
        }
    }

}
