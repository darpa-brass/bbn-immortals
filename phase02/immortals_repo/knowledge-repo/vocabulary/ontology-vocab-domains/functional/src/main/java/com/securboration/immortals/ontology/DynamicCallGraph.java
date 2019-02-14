package com.securboration.immortals.ontology;

public class DynamicCallGraph {

    private DynamicCallGraphEdge[] dynamicCallGraphEdges;

    public DynamicCallGraphEdge[] getDynamicCallGraphEdges() {
        return dynamicCallGraphEdges;
    }
    public void setDynamicCallGraphEdges(DynamicCallGraphEdge[] dynamicCallGraphEdges) {
        this.dynamicCallGraphEdges = dynamicCallGraphEdges;
    }
}