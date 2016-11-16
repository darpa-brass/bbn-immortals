package com.securboration.immortals.instantiation.annotationparser.bytecode;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;

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
        final File jarDir = new File(args[1]);
        final File outputDir = new File(args[2]);
        
        
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
                
//                Console.log(
//                    "bytecode model for jar %s:\n%s\n", 
//                    jar.getName(), 
//                    serialized
//                    );
                
                FileUtils.writeStringToFile(
                    new File(outputPath), 
                    serialized
                    );
            }
        }
    }

}
