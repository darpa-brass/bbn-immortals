package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Describes the impact of something explicitly prohibited (e.g., what " +
        "happens when we exceed some hard resource limit?)"
        )
    )
public class ProscriptiveCauseEffectAssertion extends CauseEffectAssertion {
    

}
