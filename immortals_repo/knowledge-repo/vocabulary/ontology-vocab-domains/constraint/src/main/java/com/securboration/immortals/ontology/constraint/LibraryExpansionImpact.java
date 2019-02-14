package com.securboration.immortals.ontology.constraint;

import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.property.impact.AnalysisImpact;

public class LibraryExpansionImpact extends AnalysisImpact {

    private String libraryName;
    
    private MethodAdaptation[] methodAdaptations;
    
    private FieldAdaptation fieldAdaptation;
    
    private String pathToLibrary;
    
    private Class<? extends FunctionalAspect> aspectImplemented;

    public Class<? extends FunctionalAspect> getAspectImplemented() {
        return aspectImplemented;
    }

    public void setAspectImplemented(Class<? extends FunctionalAspect> aspectImplemented) {
        this.aspectImplemented = aspectImplemented;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public String getPathToLibrary() {
        return pathToLibrary;
    }

    public void setPathToLibrary(String pathToLibrary) {
        this.pathToLibrary = pathToLibrary;
    }

    public MethodAdaptation[] getMethodAdaptations() {
        return methodAdaptations;
    }

    public void setMethodAdaptations(MethodAdaptation[] methodAdaptations) {
        this.methodAdaptations = methodAdaptations;
    }

    public FieldAdaptation getFieldAdaptation() {
        return fieldAdaptation;
    }

    public void setFieldAdaptation(FieldAdaptation fieldAdaptation) {
        this.fieldAdaptation = fieldAdaptation;
    }
}
