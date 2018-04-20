package com.securboration.immortals.ontology.bytecode;

import com.securboration.immortals.ontology.annotations.RdfsComment;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.annotations.triples.Triples;
import com.securboration.immortals.uris.Uris.owl;
import com.securboration.immortals.uris.Uris.rdf;

/**
 * The class is the fundamental compilation unit of bytecode.
 * 
 * This is a logical representation of a class. E.g., classes contain fields,
 * methods, annotations, etc.
 * 
 * @author Securboration
 *
 */
public class AClass extends ClassStructure {

    @Triples({
        @Triple(
            predicateUri=rdf.type$,
            objectUri=owl.FunctionalProperty$
            )
        })
    /**
     * A URL pointing to the .java source code that was compiled to create
     * a class instance
     * 
     * @author Securboration
     */
    private String sourceUrl;

    /**
     * A URL pointing to the .class (bytecode) that makes up this class instance
     */
    private String classUrl;

    /**
     * The internal name of the class (e.g., java/lang/String)
     */
    private String className;

    /**
     * A unique identifier for this object
     */
    private String bytecodePointer;

    /**
     * inner classes
     */
    private AClass[] innerClasses;

    /**
     * fields belonging to this class
     */
    private AField[] fields;

    /**
     * methods belonging to this class
     */
    private AMethod[] methods;
    
    /**
     * The bytecode version for this class
     */
    private BytecodeVersion bytecodeVersion;

    /**
     * Files found that this class is dependent on
     */
    private String[] dependentFiles;

    /**
     * Source code of this class
     */
    private String source;

    public AClass[] getInnerClasses() {
        return innerClasses;
    }

    public void setInnerClasses(AClass[] innerClasses) {
        this.innerClasses = innerClasses;
    }

    public AField[] getFields() {
        return fields;
    }

    public void setFields(AField[] fields) {
        this.fields = fields;
    }

    public AMethod[] getMethods() {
        return methods;
    }

    public void setMethods(AMethod[] methods) {
        this.methods = methods;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getBytecodePointer() {
        return bytecodePointer;
    }

    public void setBytecodePointer(String bytecodePointer) {
        this.bytecodePointer = bytecodePointer;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getClassUrl() {
        return classUrl;
    }

    public void setClassUrl(String classUrl) {
        this.classUrl = classUrl;
    }

    public BytecodeVersion getBytecodeVersion() {
        return bytecodeVersion;
    }

    public void setBytecodeVersion(BytecodeVersion bytecodeVersion) {
        this.bytecodeVersion = bytecodeVersion;
    }

    public String[] getDependentFiles() {
        return dependentFiles;
    }

    public void setDependentFiles(String[] dependentFiles) {
        this.dependentFiles = dependentFiles;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
