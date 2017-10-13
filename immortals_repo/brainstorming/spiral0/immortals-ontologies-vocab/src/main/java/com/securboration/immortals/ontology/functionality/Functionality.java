package com.securboration.immortals.ontology.functionality;

/**
 * A description of what a functional unit of software does (but not how it is
 * done). If we know that two different pieces of code do essentially the same
 * thing in a different way, the idea is that we can adapt our software by
 * swapping between them.
 * 
 * @author Securboration
 *
 */
public class Functionality {

    /**
     * Our abstraction of functionality comprises various atomic functional 
     * aspects
     */
    private FunctionalAspect[] functionalAspects;

    public FunctionalAspect[] getFunctionalAspects() {
        return functionalAspects;
    }

    public void setFunctionalAspects(FunctionalAspect[] functionalAspects) {
        this.functionalAspects = functionalAspects;
    }

}
