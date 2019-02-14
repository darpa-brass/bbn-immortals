package com.securboration.immortals.ontology.functionality;

/**
 * Where should this aspect be injected in order to be applied correctly?
 * e.g. AspectEncrypt should be injected in relation to SendOverNetwork,
 * specifically just before
 */
public class AspectInjectionPoint {
    
    private AspectInjectionRelation aspectInjectionRelation;
    
    private Class<? extends FunctionalAspect> aspect;

    public AspectInjectionRelation getAspectInjectionRelation() {
        return aspectInjectionRelation;
    }

    public void setAspectInjectionRelation(AspectInjectionRelation aspectInjectionRelation) {
        this.aspectInjectionRelation = aspectInjectionRelation;
    }

    public Class<? extends FunctionalAspect> getAspect() {
        return aspect;
    }

    public void setAspect(Class<? extends FunctionalAspect> aspect) {
        this.aspect = aspect;
    }
}
