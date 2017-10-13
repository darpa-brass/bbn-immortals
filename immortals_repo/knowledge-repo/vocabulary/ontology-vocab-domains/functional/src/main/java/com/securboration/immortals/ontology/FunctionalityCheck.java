package com.securboration.immortals.ontology;

import com.securboration.immortals.ontology.java.compiler.NamedClasspath;

/**
 * Describes a FunctionalityTest-annotated method
 */
public class FunctionalityCheck {
    
    // Unambiguously describes a method in a CompiledJavaSourceCode object
    private String methodPointer;
    // The type of functionality being tested during execution
    String type;
    // The test runs the described method has been invoked for
    private FunctionalityTestRun[] functionalityTestRuns;
    
    private NamedClasspath classpath;

    public NamedClasspath getClasspath() {
        return classpath;
    }

    public void setClasspath(NamedClasspath classpath) {
        this.classpath = classpath;
    }

    public String getMethodPointer() {
        return methodPointer;
    }
    public void setMethodPointer(String methodPointer) {
        this.methodPointer = methodPointer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FunctionalityTestRun[] getFunctionalityTestRuns() {
        return functionalityTestRuns;
    }

    public void setFunctionalityTestRuns(FunctionalityTestRun[] functionalityTestRuns) {
        this.functionalityTestRuns = functionalityTestRuns;
    }
}
