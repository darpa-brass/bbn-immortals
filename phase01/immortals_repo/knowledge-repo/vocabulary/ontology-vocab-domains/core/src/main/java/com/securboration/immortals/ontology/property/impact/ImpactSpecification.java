package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.functionality.dataproperties.ImpactType;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Models an impact"
        )
    )
public class ImpactSpecification {
    
    private ImpactType impact;

    
    public ImpactType getImpact() {
        return impact;
    }

    
    public void setImpact(ImpactType impact) {
        this.impact = impact;
    }

}
