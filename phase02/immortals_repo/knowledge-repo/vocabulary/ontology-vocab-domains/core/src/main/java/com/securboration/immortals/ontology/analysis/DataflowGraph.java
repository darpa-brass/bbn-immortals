package com.securboration.immortals.ontology.analysis;

/**
 * A dataflow graph is a set of dataflow edges.  Each edge, in turn, connects
 * dataflow nodes via some communication medium (e.g., softwareComponent1 
 * communicates with softwareComponent2 via a namedSocket9).
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A dataflow graph is a set of dataflow edges.  Each edge, in turn," +
    " connects dataflow nodes via some communication medium (e.g.," +
    " softwareComponent1  communicates with softwareComponent2 via a" +
    " namedSocket9).  @author jstaples ")
public class DataflowGraph {
    
    /**
     * The edges in the graph
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The edges in the graph")
    private DataflowEdge[] edges;
    
    /**
     * A human-readable description of what is in the graph
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A human-readable description of what is in the graph")
    private String humanReadableDescription;

    
    public DataflowEdge[] getEdges() {
        return edges;
    }

    
    public void setEdges(DataflowEdge[] edges) {
        this.edges = edges;
    }


    
    public String getHumanReadableDescription() {
        return humanReadableDescription;
    }


    
    public void setHumanReadableDescription(String humanReadableDescription) {
        this.humanReadableDescription = humanReadableDescription;
    }
    
}
