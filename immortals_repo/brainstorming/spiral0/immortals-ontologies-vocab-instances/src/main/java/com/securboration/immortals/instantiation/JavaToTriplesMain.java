package com.securboration.immortals.instantiation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.securboration.immortals.j2t.analysis.JavaToOwl;
import com.securboration.immortals.j2t.analysis.JavaToTriplesConfiguration;

/**
 * Converts POJOs into a schema
 * 
 * @author jstaples
 *
 */
public class JavaToTriplesMain {
 
    

    /**
     * 
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        
        JavaToTriplesConfiguration config = 
                getJavaToTriplesConfig(
                        args[2]//ontology version
                        );
        
        config.setOutputFile(new File(args[0]));//output file
        config.setOutputLanguage(args[1]);//output language
        
        config.setClassPaths(
                Arrays.asList(
                        args[3]//class paths
                        .split(",")));
        
        config.setSourcePaths(
                Arrays.asList(
                        args[4]//source paths
                        .split(",")));
        
        config.setSkipPrefixes(
                Arrays.asList(
                        args[5]//skip prefixes
                        .split(",")));
        
        JavaToOwl j2o = new JavaToOwl(config);
        j2o.analyze();

    }
    
    private static JavaToTriplesConfiguration getJavaToTriplesConfig(
            final String version
            ) {
        // TODO: other constants could come from args, for now they're hard
        // coded

        JavaToTriplesConfiguration config = new JavaToTriplesConfiguration();
        config.setNamespaceMappings(
                Arrays.asList("http://darpa.mil/immortals/ontology/" + version
                        + "# IMMoRTALS"));
        config.setTargetNamespace(
                "http://darpa.mil/immortals/ontology/" + version);
        config.setOutputFile(null);
        config.setTrimPrefixes(
                Arrays.asList("com/securboration/immortals/ontology"));

        return config;
    }

}
