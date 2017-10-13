package com.securboration.immortals.instantiation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.securboration.immortals.annotations.generator.AnnotationGenerator;
import com.securboration.immortals.annotations.generator.AnnotationGeneratorConfiguration;
import com.securboration.immortals.j2t.analysis.JavaToTriplesConfiguration;

/**
 * Converts POJOs into semantic annotations
 * 
 * @author jstaples
 *
 */
public class JavaToAnnotationsMain {
 
    

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
        
        config.setSkipPrefixes(
                Arrays.asList(
                        args[4]//skip prefixes
                        .split(",")));
        
        AnnotationGeneratorConfiguration c = 
                new AnnotationGeneratorConfiguration();
        c.setJavaToTriplesConfiguration(config);
        c.setPackagePrefix(
                args[5]//package prefix
                );
        c.setAnnotationsOutputDir(
                args[6]//output dir for generated annotations
                );
        
        AnnotationGenerator.generateAnnotations(c);
    }

}
