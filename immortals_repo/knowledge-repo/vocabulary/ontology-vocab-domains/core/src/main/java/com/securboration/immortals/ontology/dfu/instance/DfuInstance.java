package com.securboration.immortals.ontology.dfu.instance;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.property.Property;

/**
 * An instantiation of a DFU that binds what abstract things are being done by
 * the DFU to how they are implemented in bytecode
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "An instantiation of a DFU that binds what abstract things are being" +
    " done by the DFU to how they are implemented in bytecode  @author" +
    " jstaples ")
public class DfuInstance {
    
    public DfuInstance(){
        
    }
    
    /**
     * The class in which the functionality is performed
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The class in which the functionality is performed")
    private String classPointer;
    
    /**
     * The abstract functionality performed by the DFU 
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The abstract functionality performed by the DFU")
    private Class<? extends Functionality> functionalityAbstraction;
    
    /**
     * The abstract resources upon which the DFU depends, if any
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The abstract resources upon which the DFU depends, if any")
    private Class<? extends Resource>[] resourceDependencies;
    
    /**
     * The concrete resources upon which the DFU depends, if any
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The concrete resources upon which the DFU depends, if any")
    private Resource[] concreteResourceDependencies;
    
    /**
     * Properties bound directly to the DFU
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Properties bound directly to the DFU")
    private Property[] dfuProperties;
    
    /**
     * A durable reference to this DFU instance
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A durable reference to this DFU instance")
    private String tag;
    
    /**
     * The functional aspects of the DFU
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The functional aspects of the DFU")
    private FunctionalAspectInstance[] functionalAspects;

    
    public String getClassPointer() {
        return classPointer;
    }

    
    public void setClassPointer(String classPointer) {
        this.classPointer = classPointer;
    }

    
    public Class<? extends Functionality> getFunctionalityAbstraction() {
        return functionalityAbstraction;
    }

    
    public void setFunctionalityAbstraction(
            Class<? extends Functionality> functionalityAbstraction) {
        this.functionalityAbstraction = functionalityAbstraction;
    }

    
    public Class<? extends Resource>[] getResourceDependencies() {
        return resourceDependencies;
    }

    
    public void setResourceDependencies(
            Class<? extends Resource>[] resourceDependencies) {
        this.resourceDependencies = resourceDependencies;
    }

    
    public Property[] getDfuProperties() {
        return dfuProperties;
    }

    
    public void setDfuProperties(Property[] dfuProperties) {
        this.dfuProperties = dfuProperties;
    }

    
    public FunctionalAspectInstance[] getFunctionalAspects() {
        return functionalAspects;
    }

    
    public void setFunctionalAspects(FunctionalAspectInstance[] functionalAspects) {
        this.functionalAspects = functionalAspects;
    }


    
    public String getTag() {
        return tag;
    }


    
    public void setTag(String tag) {
        this.tag = tag;
    }


    
    public Resource[] getConcreteResourceDependencies() {
        return concreteResourceDependencies;
    }


    
    public void setConcreteResourceDependencies(
            Resource[] concreteResourceDependencies) {
        this.concreteResourceDependencies = concreteResourceDependencies;
    }

}
