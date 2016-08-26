package com.securboration.immortals.ontology.cp;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.ordering.ExplicitNumericOrderingMechanism;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "A specification for the behavior of software " +
        "(ie, its intent)"
        )
    )
public class SoftwareSpec {
    
    private ExplicitNumericOrderingMechanism precedenceOfSpec;

    
    public ExplicitNumericOrderingMechanism getPrecedenceOfSpec() {
        return precedenceOfSpec;
    }

    
    public void setPrecedenceOfSpec(
            ExplicitNumericOrderingMechanism precedenceOfSpec) {
        this.precedenceOfSpec = precedenceOfSpec;
    }
    
}
