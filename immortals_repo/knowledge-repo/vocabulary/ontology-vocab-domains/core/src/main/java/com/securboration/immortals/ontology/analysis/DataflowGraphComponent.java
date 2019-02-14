package com.securboration.immortals.ontology.analysis;

import com.securboration.immortals.ontology.annotations.RdfsComment;

@RdfsComment("Describes a component of a dataflow graph, whether it be a node or an edge connected it")
public class DataflowGraphComponent {

    /**
     * A human-readable description of what is in the graph
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
            "A human-readable description of what is in the graph")
    private String humanReadableDescription;

    public String getHumanReadableDescription() {
        return humanReadableDescription;
    }

    public void setHumanReadableDescription(String humanReadableDescription) {
        this.humanReadableDescription = humanReadableDescription;
    }
}
