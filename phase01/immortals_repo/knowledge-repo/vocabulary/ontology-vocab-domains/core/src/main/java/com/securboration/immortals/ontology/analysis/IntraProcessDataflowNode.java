package com.securboration.immortals.ontology.analysis;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "A dataflow node that comes from within this process (e.g., " +
        "via a function call)"
        )
    )
public class IntraProcessDataflowNode extends DataflowNode {
    
    

}
