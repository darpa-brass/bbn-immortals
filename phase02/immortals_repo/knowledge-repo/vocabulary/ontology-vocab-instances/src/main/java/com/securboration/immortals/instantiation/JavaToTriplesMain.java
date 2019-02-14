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
                new JavaToTriplesConfiguration(
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

}
