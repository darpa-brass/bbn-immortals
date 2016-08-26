package com.securboration.immortals.annotationparser.main;

import java.io.File;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.securboration.immortals.instantiation.annotationparser.bytecode.Console;
import com.securboration.immortals.instantiation.annotationparser.traversal.AnnotationParser;
import com.securboration.immortals.instantiation.annotationparser.traversal.JarTraverser;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.semanticweaver.ObjectMapper;

public class MainTest {
    
    private static String[] getTestArgs(){
        
        return new String[]{
                "r2.0.0",
                "C:/Users/Securboration/Desktop/code/immortals/trunk/knowledge-repo/vocabulary/dsl-generate/target/test-classes"
        };
        
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
        args = getTestArgs();//TODO
        
        final String version = args[0];
        final File dirContainingClasses = new File(args[1]);
        
        {//JAR mode
            Console.log(
                "searching %s",
                dirContainingClasses.getAbsolutePath()
                );
            
            ObjectToTriplesConfiguration config = 
                    new ObjectToTriplesConfiguration(version);
            config.setAddMetadata(false);
            
            AnnotationParser parser = 
                    new AnnotationParser(config);
            
            JarTraverser.traverseNakedClasspath(
                dirContainingClasses, 
                parser
                );
            
            Model jarModel = ModelFactory.createDefaultModel();
            
            ObjectMapper mapper = config.getMapper();
            
            for(Object o:mapper.getObjectsToSerialize()){
                Model objectModel = ObjectToTriples.convert(config, o);
                
                jarModel.add(objectModel);
            }
            
            OntologyHelper.addMetadata(
                config, 
                jarModel, 
                config.getTargetNamespace(), 
                config.getOutputFile()
                );
            
//            //TODO
//            if(true){throw new RuntimeException("intentional");}
            
            final String serialized = 
                    OntologyHelper.serializeModel(jarModel, "TTL");
            
            Console.log("model for classpath:\n%s\n",serialized);
        }
    }

}
