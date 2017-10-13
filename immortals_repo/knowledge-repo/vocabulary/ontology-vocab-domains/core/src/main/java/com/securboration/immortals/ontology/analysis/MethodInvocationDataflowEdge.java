package com.securboration.immortals.ontology.analysis;

import com.securboration.immortals.ontology.dfu.instance.ArgToSemanticTypeBinding;

/**
 * Describes the flow of data between two MethodInvocationDataflowNode's
 */
public class MethodInvocationDataflowEdge extends DataflowEdge {
    
    // What is the semantic binding of the data before being transferred
    private ArgToSemanticTypeBinding entryBinding;
    // What is the semantic binding of the data after being transferred
    private ArgToSemanticTypeBinding exitBinding;

    public ArgToSemanticTypeBinding getEntryBinding() {
        return entryBinding;
    }

    public void setEntryBinding(ArgToSemanticTypeBinding entryBinding) {
        this.entryBinding = entryBinding;
    }

    public ArgToSemanticTypeBinding getExitBinding() {
        return exitBinding;
    }

    public void setExitBinding(ArgToSemanticTypeBinding exitBinding) {
        this.exitBinding = exitBinding;
    }
}
