package com.securboration.immortals.o2t;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.securboration.immortals.o2t.analysis.ObjectToTriples.NamingContext;
import com.securboration.immortals.semanticweaver.ObjectMapper;

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

    private ClassLoader classloader = 
            ObjectToTriplesConfiguration.class.getClassLoader();

    private Collection<String> namespaceMappings = new ArrayList<>();

    private String targetNamespace = 
            "http://securboration.com/immortals/ontology/r1.0.0.SNAPSHOT";

    private File outputFile = 
            new File("./immortals-" + UUID.randomUUID().toString() + ".ttl");

    private String outputLanguage = "Turtle";

    private Collection<String> trimPrefixes = new ArrayList<>();

    private boolean autoGenerateAdditionalPrefixes = true;
    
    private boolean flattenArrays = true;
    
    private boolean addDomainRangeToProperties = true;
    
    private boolean addDisjointAssertions = false;
    
    private boolean addKeys = false;
    
    private boolean addPojoProvenance = false;
    
    private boolean includeTypeWithProperties = false;
    
    private boolean addFieldRestrictions = true;
    
    private boolean validateOntology = 
            System.getProperty("validateOntology") != null;

    private String version = "r1.0.0.SNAPSHOT";
    
    private NamingContext namingContext = new NamingContext();
    
    private ObjectMapper mapper = new ObjectMapper();
    
    private boolean addMetadata = false;
    
    private ObjectTranslator objectTranslator = new ObjectTranslatorImpl();
    
    private final Collection<Field> ignoredFields = new HashSet<>();

    public Logger getLog() {
        return log;
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
    
    public ObjectToTriplesConfiguration(
            final String version
            ) {
        // TODO: could do more customization, for now use sane defaults
        
        this.setNamespaceMappings(
                Arrays.asList("http://darpa.mil/immortals/ontology/" + version
                        + "# IMMoRTALS"));
        this.setTargetNamespace(
                "http://darpa.mil/immortals/ontology/" + version);
        this.setOutputFile(null);
        this.setTrimPrefixes(
                Arrays.asList("com/securboration/immortals/ontology"));
    }

    public boolean shouldFlattenArrays() {
        return flattenArrays;
    }

    public void setFlattenArrays(boolean flattenArrays) {
        this.flattenArrays = flattenArrays;
    }

    
    public ObjectMapper getMapper() {
        return mapper;
    }

    
    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    
    public boolean isAddMetadata() {
        return addMetadata;
    }

    
    public void setAddMetadata(boolean addMetadata) {
        this.addMetadata = addMetadata;
    }

    
    public boolean isAddDomainRangeToProperties() {
        return addDomainRangeToProperties;
    }

    
    public void setAddDomainRangeToProperties(boolean addDomainRangeToProperties) {
        this.addDomainRangeToProperties = addDomainRangeToProperties;
    }

    
    public boolean isAddDisjointAssertions() {
        return addDisjointAssertions;
    }

    
    public void setAddDisjointAssertions(boolean addDisjointAssertions) {
        this.addDisjointAssertions = addDisjointAssertions;
    }

    
    public boolean isValidateOntology() {
        return validateOntology;
    }

    
    public void setValidateOntology(boolean validateOntology) {
        this.validateOntology = validateOntology;
    }

    
    public boolean isAddKeys() {
        return addKeys;
    }

    
    public void setAddKeys(boolean addKeys) {
        this.addKeys = addKeys;
    }

    
    public boolean isAddPojoProvenance() {
        return addPojoProvenance;
    }

    
    public void setAddPojoProvenance(boolean addPojoProvenance) {
        this.addPojoProvenance = addPojoProvenance;
    }

    
    public boolean isIncludeTypeWithProperties() {
        return includeTypeWithProperties;
    }

    
    public void setIncludeTypeWithProperties(boolean includeTypeWithProperties) {
        this.includeTypeWithProperties = includeTypeWithProperties;
    }

    
    public boolean isAddFieldRestrictions() {
        return addFieldRestrictions;
    }

    
    public void setAddFieldRestrictions(boolean addFieldRestrictions) {
        this.addFieldRestrictions = addFieldRestrictions;
    }
    
    
    public ObjectTranslator getObjectTranslator() {
        return objectTranslator;
    }

    
    public void setObjectTranslator(ObjectTranslator objectTranslator) {
        this.objectTranslator = objectTranslator;
    }

    
    public Collection<Field> getIgnoredFields() {
        return ignoredFields;
    }

}
