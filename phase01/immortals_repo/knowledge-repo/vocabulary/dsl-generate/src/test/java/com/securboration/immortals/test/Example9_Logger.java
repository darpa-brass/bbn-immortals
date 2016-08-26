package com.securboration.immortals.test;

import java.io.PrintStream;

import com.securboration.immortals.ontology.functionality.logger.AspectLog;
import com.securboration.immortals.ontology.functionality.logger.AspectLoggerCleanup;
import com.securboration.immortals.ontology.functionality.logger.AspectLoggerInitialize;
import com.securboration.immortals.ontology.functionality.logger.Logger;
import com.securboration.immortals.ontology.resources.FileSystemResource;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;

/**
 * A simple logger DFU
 * 
 * @author Securboration
 *
 */
@DfuAnnotation(
    functionalityBeingPerformed = Logger.class,
    resourceDependencies={
            FileSystemResource.class
            }
    )
public class Example9_Logger {
    
    private PrintStream ps = System.out;
    
    @FunctionalAspectAnnotation(
        aspect=AspectLoggerInitialize.class
        )
    public void initialize(){}
    
    @FunctionalAspectAnnotation(
        aspect=AspectLoggerCleanup.class
        )
    public void cleanup(){}
    
    @FunctionalAspectAnnotation(
        aspect=AspectLog.class
        )
    public void log(String message){ps.println(message);}
    
}
