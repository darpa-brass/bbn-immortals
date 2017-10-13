package com.securboration.immortals.ontology.java.compiler;

import com.securboration.immortals.ontology.bytecode.application.Classpath;

import java.util.HashSet;
import java.util.Set;


/**
 * Adds a layer of abstraction between Classpaths and what generates them, so both android
 * and Java projects (and presumably other JVM classpath generating builds) are handled 
 * @author Clayton
 *
 */
public class NamedClasspath extends Classpath {
    
    private String classpathName;

    // Using HashSet for optimized search, might need to go back to array for memory purposes
    private Set<String> elementHashValues = new HashSet<>();
    
    public String getClasspathName() {
        return classpathName;
    }
    
    public Set<String> getElementHashValues() { return  elementHashValues; }

    public void setClasspathName(String classpathName) {
        this.classpathName = classpathName;
    }
    
    public boolean addElementHashValue(String elementHashValue) { return this.elementHashValues.add(elementHashValue); }
    

}
