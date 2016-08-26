package com.securboration.immortals.instantiation;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;

import com.securboration.immortals.deployment.parser3.Parser;
import com.securboration.immortals.i2t.ontology.ModelToTriples;
import com.securboration.immortals.i2t.ontology.ModelToTriples.NamingContext;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;

/**
 * Converts a WebGME JSON model into triples.
 * 
 * @author jstaples
 *
 */
public class DeploymentToTriplesMain {
    
    /**
     * 
     * @param args
     * 
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

//        args = new String[]{//TODO
//                "./output/parsed-from-webgme-models",
//                "r2.0.0",
//                "../../..",
//                "models/sample_android/resource/webgme/immortals_kds_dm_v02_a02.json"
//        };
        
        final String outputBasePath = args[0];
        final String version = args[1];
        final String projectRoot = args[2];//absolute on local FS
        final String deploymentFileToIngest = args[3];//relative to project root
        
        final File jsonFile = 
                getFile(projectRoot,deploymentFileToIngest);
        final String json = 
                FileUtils.readFileToString(jsonFile);
        
        
        ObjectToTriplesConfiguration config = 
                new ObjectToTriplesConfiguration(version);
        
//        //use the kludge parser
//        {
//            final String outputPath = 
//                    outputBasePath + "/deploymentOld/" + jsonFile.getName() + ".ttl";
//            
//            ParserImpl parser = new ParserImpl();
//            parser.parse(json);
//            
//            Model m = 
//                    ModelToTriples.convert(
//                            config, 
//                            parser.getTypes(), //type hierarchy
//                            parser.getInstances(), //instances
//                            new NamingContext()
//                            );
//            
//            System.out.println("writing model @" + outputPath);
//            
//            FileUtils.writeStringToFile(
//                    new File(outputPath),
//                    OntologyHelper.serializeModel(m, "Turtle"));
//        }
        
        //use the new parser
        {
            final String outputPath = 
                    outputBasePath + "/deployment/" + jsonFile.getName() + ".ttl";
            
            Parser parser = new Parser();
            parser.parse(json);
            
            config.setOutputFile(new File(outputPath));
            
            Model m = 
                    ModelToTriples.convert(
                            config, 
                            parser.getTypes(), //type hierarchy
                            parser.getInstances(), //instances
                            new NamingContext()
                            );
            
            System.out.println("writing model @" + outputPath);
            
//            FileUtils.writeStringToFile(
//                    new File(outputPath),
//                    OntologyHelper.serializeModel(m, "Turtle"));
        }
    }
    

    
    private static File getFile(
            String projectRoot,
            String pathRelativeToProjectRoot
            ){
        String path = projectRoot + "/" + pathRelativeToProjectRoot;
        
        return new File(path);
    }

}
