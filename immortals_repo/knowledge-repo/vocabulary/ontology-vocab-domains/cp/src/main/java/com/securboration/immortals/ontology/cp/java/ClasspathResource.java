package com.securboration.immortals.ontology.cp.java;

import com.securboration.immortals.ontology.core.Resource;

/**
 * A resource on a classpath.  I.e., something that can be accessed by a 
 * classloader.
 * 
 * @author Securboration
 *
 */
public class ClasspathResource extends Resource {

    /**
     * The actual resource bytes
     */
    private byte[] bytes;
    
    /**
     * The name of the class on the classpath.  E.g., java.lang.String
     */
    private String classpathName;
    
    
    
}
