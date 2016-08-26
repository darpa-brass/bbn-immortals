package com.securboration.immortals.annotations.generator;

import com.securboration.immortals.j2t.analysis.JavaToTriplesConfiguration;

/**
 * Configuration for the annotation generator. All fields have sane defaults.
 * 
 * @author jstaples
 *
 */
public class AnnotationGeneratorConfiguration {
    
    public AnnotationGeneratorConfiguration() {
    }
    
    private JavaToTriplesConfiguration javaToTriplesConfiguration;
    
    private String packagePrefix = "com.securboration.immortals.adsl";
    
    private String annotationsOutputDir = null;

    
    public String getAnnotationsOutputDir() {
        return annotationsOutputDir;
    }

    public void setAnnotationsOutputDir(String annotationsOutputDir) {
        this.annotationsOutputDir = annotationsOutputDir;
    }

    public String getPackagePrefix() {
        return packagePrefix;
    }

    public void setPackagePrefix(String packagePrefix) {
        this.packagePrefix = packagePrefix;
    }

    public JavaToTriplesConfiguration getJavaToTriplesConfiguration() {
        return javaToTriplesConfiguration;
    }

    public void setJavaToTriplesConfiguration(
            JavaToTriplesConfiguration javaToTriplesConfiguration) {
        this.javaToTriplesConfiguration = javaToTriplesConfiguration;
    }
    
    

}
