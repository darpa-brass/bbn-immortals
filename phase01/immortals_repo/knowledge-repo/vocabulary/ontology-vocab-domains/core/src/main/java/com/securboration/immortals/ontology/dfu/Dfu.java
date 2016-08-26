package com.securboration.immortals.ontology.dfu;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.lang.CompiledCodeUnit;

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
    private CompiledCodeUnit codeUnit;
    
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

    public Class<? extends Resource>[] getResourceDependencies() {
        return resourceDependencies;
    }

    public void setResourceDependencies(
            Class<? extends Resource>[] resourceDependencies) {
        this.resourceDependencies = resourceDependencies;
    }

    public CompiledCodeUnit getCodeUnit() {
        return codeUnit;
    }

    public void setCodeUnit(CompiledCodeUnit codeUnit) {
        this.codeUnit = codeUnit;
    }

}
