package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Describes how to work around a condition that is explicitly " +
        "prohibited (e.g., what can we do to mitigate using too much of a " +
        "certain resource?)"
        )
    )
public class PrescriptiveCauseEffectAssertion extends CauseEffectAssertion {
    

}
