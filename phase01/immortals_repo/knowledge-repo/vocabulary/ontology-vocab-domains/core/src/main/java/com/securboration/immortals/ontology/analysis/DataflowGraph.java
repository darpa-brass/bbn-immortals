package com.securboration.immortals.ontology.analysis;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "A dataflow graph is a set of dataflow edges"
        )
    )
public class DataflowGraph {
    
    private DataflowEdge[] edges;
    
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
