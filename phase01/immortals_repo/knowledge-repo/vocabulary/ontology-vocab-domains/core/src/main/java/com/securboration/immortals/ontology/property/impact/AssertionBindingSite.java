package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Models something to which an assertion can bind"
        )
    )
public class AssertionBindingSite {
    
    private String humanReadableDescription;

    public String getHumanReadableDescription() {
        return humanReadableDescription;
    }
    
    public void setHumanReadableDescription(String humanReadableDescription) {
        this.humanReadableDescription = humanReadableDescription;
    }
    
    
}
