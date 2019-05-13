package com.securboration.immortals.ontology.analysis;

import com.securboration.immortals.ontology.dfu.instance.ArgToSemanticTypeBinding;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof MethodInvocationDataflowEdge)) {
            return false;
        }

        MethodInvocationDataflowEdge methodEdge = (MethodInvocationDataflowEdge) o;
        if (getProducer() == null && methodEdge.getProducer() != null) {
            return false;
        }
        if (getConsumer() == null && methodEdge.getConsumer() != null) {
            return false;
        }

        return Objects.equals(getDataTypeCommunicated(), methodEdge.getDataTypeCommunicated()) &&
                Objects.equals(getProducer(), methodEdge.getProducer()) &&
                Objects.equals(getConsumer(), methodEdge.getConsumer());

    }

    @Override
    public int hashCode() {
        return Objects.hash(getDataTypeCommunicated(), getProducer(), getConsumer());
    }
}
