package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.constraint.ResourceImpactType;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Models an impact on a resource"
        )
    )
public class ResourceImpact extends ImpactStatement {
    
    private ResourceImpactType impactOnResource;
    private Class<? extends Resource> impactedResource;
    
    public ResourceImpactType getImpactOnResource() {
        return impactOnResource;
    }
    
    public void setImpactOnResource(ResourceImpactType impactOnResource) {
        this.impactOnResource = impactOnResource;
    }
    
    public Class<? extends Resource> getImpactedResource() {
        return impactedResource;
    }
    
    public void setImpactedResource(Class<? extends Resource> impactedResource) {
        this.impactedResource = impactedResource;
    }

}
