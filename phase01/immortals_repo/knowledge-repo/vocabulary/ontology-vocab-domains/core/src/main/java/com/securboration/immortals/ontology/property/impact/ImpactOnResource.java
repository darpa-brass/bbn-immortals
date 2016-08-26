package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Models the impact of a property on a resource"
        )
    )
public class ImpactOnResource extends ImpactSpecification {
    
    private Class<? extends Resource> impactedResource;

    
    public Class<? extends Resource> getImpactedResource() {
        return impactedResource;
    }

    
    public void setImpactedResource(Class<? extends Resource> impactedResource) {
        this.impactedResource = impactedResource;
    }

}
