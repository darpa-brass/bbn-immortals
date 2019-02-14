package com.securboration.immortals.ontology.pattern.spec;

import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Functionality;

/**
 * Describes the mechanics behind using a specific functional aspect in a
 * specific library
 * 
 * @author jstaples
 *
 */
public class LibraryFunctionalAspectSpec {
    
    /**
     * Uniquely and durably identifies an instance of a concept
     */
    private String durableId;
    
    /**
     * The various components of the spec.  For example, one component might
     * be responsible for declaration, another for initialization, and another 
     * for doing work.
     */
    private SpecComponent[] component;
    
    /**
     * The usage paradigm for consuming the specified functionality
     */
    private AbstractUsageParadigm usageParadigm;
    
    /**
     * The aspect to which the spec applies
     */
    private Class<? extends FunctionalAspect> aspect;
    
    /**
     * The functionality to which the bound aspect belongs
     */
    private Class<? extends Functionality> functionality;
    
    /**
     * A gradle-style coordinate that uniquely identifies a library to which
     * this spec applies.  E.g., commons-io:commons-io:2.4
     */
    private String libraryCoordinateTag;
    
    public LibraryFunctionalAspectSpec(){}

    
    public String getLibraryCoordinateTag() {
        return libraryCoordinateTag;
    }


    
    public void setLibraryCoordinateTag(String libraryCoordinateTag) {
        this.libraryCoordinateTag = libraryCoordinateTag;
    }


    
    public Class<? extends Functionality> getFunctionality() {
        return functionality;
    }


    
    public void setFunctionality(Class<? extends Functionality> functionality) {
        this.functionality = functionality;
    }


    
    public Class<? extends FunctionalAspect> getAspect() {
        return aspect;
    }


    
    public void setAspect(Class<? extends FunctionalAspect> aspect) {
        this.aspect = aspect;
    }


    
    public SpecComponent[] getComponent() {
        return component;
    }


    
    public void setComponent(SpecComponent[] component) {
        this.component = component;
    }


    
    public String getDurableId() {
        return durableId;
    }


    
    public void setDurableId(String durableId) {
        this.durableId = durableId;
    }


    
    public AbstractUsageParadigm getUsageParadigm() {
        return usageParadigm;
    }


    
    public void setUsageParadigm(AbstractUsageParadigm usageParadigm) {
        this.usageParadigm = usageParadigm;
    }

}
