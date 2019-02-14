package com.securboration.immortals.ontology.analysis.cg;

import com.securboration.immortals.ontology.measurement.CodeUnitPointer;

/**
 * An edge in a call graph
 * 
 * @author jstaples
 *
 */
public class CallGraphEdge {

    /**
     * The method from which the call is made
     */
    private CodeUnitPointer originMethod;
    
    /**
     * The target of the call
     */
    private CodeUnitPointer calledMethod;

    
    public CodeUnitPointer getOriginMethod() {
        return originMethod;
    }

    
    public void setOriginMethod(CodeUnitPointer originMethod) {
        this.originMethod = originMethod;
    }

    
    public CodeUnitPointer getCalledMethod() {
        return calledMethod;
    }

    
    public void setCalledMethod(CodeUnitPointer calledMethod) {
        this.calledMethod = calledMethod;
    }

}
