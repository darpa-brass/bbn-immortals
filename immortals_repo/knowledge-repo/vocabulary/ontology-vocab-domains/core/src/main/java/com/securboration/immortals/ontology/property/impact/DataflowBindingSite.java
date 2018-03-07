package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.analysis.DataflowEdge;

public class DataflowBindingSite extends AssertionBindingSite {
    
    private DataflowEdge dataflow;

    public DataflowEdge getEdge() {
        return dataflow;
    }

    public void setEdge(DataflowEdge edge) {
        this.dataflow = edge;
    }
}
