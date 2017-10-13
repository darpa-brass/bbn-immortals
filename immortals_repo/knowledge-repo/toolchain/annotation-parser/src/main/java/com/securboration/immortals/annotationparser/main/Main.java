package com.securboration.immortals.annotationparser.main;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.securboration.immortals.instantiation.annotationparser.bytecode.Console;
import com.securboration.immortals.instantiation.annotationparser.traversal.AnnotationParser;
import com.securboration.immortals.instantiation.annotationparser.traversal.JarTraverser;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.semanticweaver.ObjectMapper;

public class Main {
    
    /**
     * args[0]: version
     * args[1]: a dir to recursively traverse containing JARs to process
     * args[2]: an output dir
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        
        final String version = args[0];
        final String[] jarDirs = args[1].split(",");
        final File outputDir = new File(args[2]);
        
        for(String dir:jarDirs){
            
            final File jarDir = new File(dir);
            
            //JAR mode
            Console.log("searching %s",jarDir.getAbsolutePath());
            
            if(!jarDir.exists()){
                Console.log(
                    "jar dir does not exist, skipping annotation parsing " +
                    "of %s\n", 
                    jarDir.getAbsolutePath()
                    );
                
                return;
            }
            
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
                
                AnnotationParser parser = 
                        new AnnotationParser(config);
                
                JarTraverser.traverseJar(jar, parser);
                
                final String outputPath = 
                        outputDir + "/" + jar.getName() + ".parsed.ttl";
                
                Model jarModel = ModelFactory.createDefaultModel();
                
                ObjectMapper mapper = config.getMapper();
                
                if(mapper.getObjectsToSerialize().size() == 0){
                    continue;
                }
                
                for(Object o:mapper.getObjectsToSerialize()){
                    Model objectModel = ObjectToTriples.convert(config, o);
                    
                    jarModel.add(objectModel);
                }
                
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
                
                Console.log(
                    "model for jar %s:\n%s\n", 
                    jar.getName(), 
                    serialized
                    );
                
                FileUtils.writeStringToFile(
                    new File(outputPath), 
                    serialized
                    );
            }
        }
    }

}
