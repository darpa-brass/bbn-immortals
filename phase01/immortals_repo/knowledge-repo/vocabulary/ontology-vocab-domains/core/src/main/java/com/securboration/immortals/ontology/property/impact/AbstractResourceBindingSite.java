package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "A type of resource to which assertions can bind"
        )
    )
public class AbstractResourceBindingSite extends AssertionBindingSite {
    
    private Class<? extends Resource> resourceType;

    
    public Class<? extends Resource> getResourceType() {
        return resourceType;
    }

    
    public void setResourceType(Class<? extends Resource> resourceType) {
        this.resourceType = resourceType;
    }
    
    
}
