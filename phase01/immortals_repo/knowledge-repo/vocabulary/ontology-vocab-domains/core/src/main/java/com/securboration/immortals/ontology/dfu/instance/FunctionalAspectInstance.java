package com.securboration.immortals.ontology.dfu.instance;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.property.Property;

/**
 * An instantiation of an abstract Functional Aspect
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "An instantiation of an abstract Functional Aspect  @author jstaples ")
public class FunctionalAspectInstance {
    
    /**
     * A pointer to the method implementing the functional aspect
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A pointer to the method implementing the functional aspect")
    private String methodPointer;
    
    /**
     * The properties of the functional aspect
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The properties of the functional aspect")
    private Property[] properties;
    
    /**
     * The resources upon which the aspect depends, if any
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The resources upon which the aspect depends, if any")
    private Class<? extends Resource>[] resourceDependencies;
    
    /**
     * The abstract functional aspect implemented
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The abstract functional aspect implemented")
    private Class<? extends FunctionalAspect> abstractAspect;
    
    /**
     * The arg mappings
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The arg mappings")
    private ArgToSemanticTypeBinding[] argsToSemanticTypes;
    
    /**
     * The return value mappings
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The return value mappings")
    private ReturnValueToSemanticTypeBinding returnValueToSemanticType;
    
    /**
     * A recipe for using the aspect
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A recipe for using the aspect")
    private String recipe;

    
    public String getMethodPointer() {
        return methodPointer;
    }

    
    public void setMethodPointer(String methodPointer) {
        this.methodPointer = methodPointer;
    }

    
    public Property[] getProperties() {
        return properties;
    }

    
    public void setProperties(Property[] properties) {
        this.properties = properties;
    }

    
    public Class<? extends FunctionalAspect> getAbstractAspect() {
        return abstractAspect;
    }

    
    public void setAbstractAspect(
            Class<? extends FunctionalAspect> abstractAspect) {
        this.abstractAspect = abstractAspect;
    }

    
    public ArgToSemanticTypeBinding[] getArgsToSemanticTypes() {
        return argsToSemanticTypes;
    }

    
    public void setArgsToSemanticTypes(
            ArgToSemanticTypeBinding[] argsToSemanticTypes) {
        this.argsToSemanticTypes = argsToSemanticTypes;
    }

    
    public ReturnValueToSemanticTypeBinding getReturnValueToSemanticType() {
        return returnValueToSemanticType;
    }

    
    public void setReturnValueToSemanticType(
            ReturnValueToSemanticTypeBinding returnValueToSemanticType) {
        this.returnValueToSemanticType = returnValueToSemanticType;
    }
    
    public Class<? extends Resource>[] getResourceDependencies() {
        return resourceDependencies;
    }


    
    public void setResourceDependencies(
            Class<? extends Resource>[] resourceDependencies) {
        this.resourceDependencies = resourceDependencies;
    }


    
    public String getRecipe() {
        return recipe;
    }


    
    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }
    
    
}
