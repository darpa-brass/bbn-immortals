package com.securboration.immortals.ontology.dfu;

import com.securboration.immortals.ontology.bytecode.ClassStructure;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.Functionality;

/**
 * An abstraction that binds what is being done to how it is implemented in
 * bytecode
 * 
 * @author Securboration
 *
 */
public class Dfu {

    /**
     * The functionality performed by the DFU
     */
    private Functionality functionalityBeingPerformed;
    
    /**
     * The location in code where the functionality is performed
     */
    private ClassStructure bytecodeLinkage;
    
    /**
     * The resources upon which the DFU depends, if any
     */
    private Class<? extends Resource>[] resourceDependencies;

    public Functionality getFunctionalityBeingPerformed() {
        return functionalityBeingPerformed;
    }

    public void setFunctionalityBeingPerformed(
            Functionality functionalityBeingPerformed) {
        this.functionalityBeingPerformed = functionalityBeingPerformed;
    }

    public ClassStructure getBytecodeLinkage() {
        return bytecodeLinkage;
    }

    public void setBytecodeLinkage(ClassStructure bytecodeLinkage) {
        this.bytecodeLinkage = bytecodeLinkage;
    }

    public Class<? extends Resource>[] getResourceDependencies() {
        return resourceDependencies;
    }

    public void setResourceDependencies(
            Class<? extends Resource>[] resourceDependencies) {
        this.resourceDependencies = resourceDependencies;
    }

}
