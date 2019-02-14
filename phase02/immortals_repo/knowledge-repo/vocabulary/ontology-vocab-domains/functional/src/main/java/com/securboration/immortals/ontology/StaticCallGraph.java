package com.securboration.immortals.ontology;

public class StaticCallGraph {

    private StaticCallGraphEdge[] staticCallGraphEdges;

    public StaticCallGraphEdge[] getStaticCallGraphEdges() {
        return staticCallGraphEdges;
    }

    public void setStaticCallGraphEdges(StaticCallGraphEdge[] _staticCallGraphEdges) {
        staticCallGraphEdges = _staticCallGraphEdges;
    }
}