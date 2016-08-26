package com.securboration.immortals.ontology.ordering;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Describes the ordering of an item by precedence.  " +
        "Lower numbers come first."
        )
    )
public class ExplicitNumericOrderingMechanism extends OrderingMechanism {

    private int precedence;

    
    public int getPrecedence() {
        return precedence;
    }

    
    public void setPrecedence(int precedence) {
        this.precedence = precedence;
    }
    
}
