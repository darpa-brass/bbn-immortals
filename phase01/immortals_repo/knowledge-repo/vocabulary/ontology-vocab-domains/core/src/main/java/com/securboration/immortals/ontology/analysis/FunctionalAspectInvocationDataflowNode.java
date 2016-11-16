package com.securboration.immortals.ontology.analysis;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "A call to a functional aspect"
        )
    )
public class FunctionalAspectInvocationDataflowNode extends MethodInvocationDataflowNode {
    
    private FunctionalAspect aspectImplemented;

    
    public FunctionalAspect getAspectImplemented() {
        return aspectImplemented;
    }
    
    public void setAspectImplemented(FunctionalAspect aspectImplemented) {
        this.aspectImplemented = aspectImplemented;
    }
    
}
