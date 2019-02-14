package com.securboration.immortals.ontology.analysis.profiling.properties;

import com.securboration.immortals.ontology.analysis.cg.CallGraphEdge;

/**
 * A call graph is a set of edges. Each edge represents an observed call of a
 * destination method from an origin method.
 * 
 * @author jstaples
 *
 */
public class DynamicCallGraph extends MeasuredProperty {

    /**
     * The invocations that were observed during execution
     */
    private CallGraphEdge[] observedInvocations;

    
    public CallGraphEdge[] getObservedInvocations() {
        return observedInvocations;
    }

    
    public void setObservedInvocations(CallGraphEdge[] observedInvocations) {
        this.observedInvocations = observedInvocations;
    }

}
