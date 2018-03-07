package com.securboration.immortals.ontology.constraint;

import com.securboration.immortals.ontology.analysis.DataflowEdge;
import com.securboration.immortals.ontology.property.impact.CriterionStatement;

public class DataflowViolation {

    private DataflowEdge dataflow;

    public DataflowEdge getDataflow() {
        return dataflow;
    }

    public void setDataflow(DataflowEdge dataflow) {
        this.dataflow = dataflow;
    }

}