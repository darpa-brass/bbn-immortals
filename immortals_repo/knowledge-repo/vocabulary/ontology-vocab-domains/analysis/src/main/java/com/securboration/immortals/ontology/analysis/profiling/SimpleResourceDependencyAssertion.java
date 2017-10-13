package com.securboration.immortals.ontology.analysis.profiling;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.measurement.CodeUnitPointer;

/**
 * Mechanism for asserting a simple (boolean) dependency of a code unit upon a
 * resource
 * 
 * @author jstaples
 *
 */
public class SimpleResourceDependencyAssertion {
    
    /**
     * The code unit about which the assertion is made. I.e., the subject of the
     * assertion.
     */
    private CodeUnitPointer codeUnit;
    
    /**
     * The resource upon which the code unit depends. I.e., the object of the
     * assertion.
     */
    private Class<? extends Resource> dependency;

    
    public CodeUnitPointer getCodeUnit() {
        return codeUnit;
    }

    
    public void setCodeUnit(CodeUnitPointer codeUnit) {
        this.codeUnit = codeUnit;
    }

    
    public Class<? extends Resource> getDependency() {
        return dependency;
    }

    
    public void setDependency(Class<? extends Resource> dependency) {
        this.dependency = dependency;
    }

}
