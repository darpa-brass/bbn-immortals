package com.securboration.immortals.o2t;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.securboration.immortals.o2t.analysis.ObjectToTriples.NamingContext;

/**
 * Configuration for the object-to-triples conversion process. All fields have
 * sane defaults.
 * 
 * @author jstaples
 *
 */
public class ObjectToTriplesConfiguration {
    private Logger log = 
            LogManager.getLogger(ObjectToTriplesConfiguration.class);

    private ClassLoader classloader = ObjectToTriplesConfiguration.class
            .getClassLoader();

    private Collection<String> namespaceMappings = new ArrayList<>();

    private String targetNamespace = "http://securboration.com/immortals/ontology/r1.0.0.SNAPSHOT";

    private File outputFile = new File("./immortals-" + UUID.randomUUID().toString() + ".ttl");

    private String outputLanguage = "Turtle";

    private Collection<String> trimPrefixes = new ArrayList<>();

    private boolean autoGenerateAdditionalPrefixes = true;

    private String version = "r1.0.0.SNAPSHOT";
    
    private NamingContext namingContext = new NamingContext();

    public Logger getLog() {
        return log;
    }

    public ObjectToTriplesConfiguration() {
        super();

    }

    public ClassLoader getClassloader() {
        return classloader;
    }

    public void setClassloader(ClassLoader classloader) {
        this.classloader = classloader;
    }

    public Collection<String> getNamespaceMappings() {
        return namespaceMappings;
    }

    public void setNamespaceMappings(Collection<String> namespaceMappings) {
        this.namespaceMappings = namespaceMappings;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public String getOutputLanguage() {
        return outputLanguage;
    }

    public void setOutputLanguage(String outputLanguage) {
        this.outputLanguage = outputLanguage;
    }

    public Collection<String> getTrimPrefixes() {
        return trimPrefixes;
    }

    public void setTrimPrefixes(Collection<String> trimPrefixes) {
        this.trimPrefixes = trimPrefixes;
    }

    public boolean isAutoGenerateAdditionalPrefixes() {
        return autoGenerateAdditionalPrefixes;
    }

    public void setAutoGenerateAdditionalPrefixes(
            boolean autoGenerateAdditionalPrefixes) {
        this.autoGenerateAdditionalPrefixes = autoGenerateAdditionalPrefixes;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public NamingContext getNamingContext() {
        return namingContext;
    }

    public void setNamingContext(NamingContext namingContext) {
        this.namingContext = namingContext;
    }

}
