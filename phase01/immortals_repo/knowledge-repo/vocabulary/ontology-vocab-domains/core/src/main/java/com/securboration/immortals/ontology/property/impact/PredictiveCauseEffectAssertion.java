package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Describes the expected but not necessarily observed impact of " +
        "one thing on another (e.g., when memory footprint increases, so " +
        "does bandwidth consumed)"
        )
    )
public class PredictiveCauseEffectAssertion extends CauseEffectAssertion {
    

}
