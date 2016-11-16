package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "A specific resource instance to which assertions can bind"
        )
    )
public class ResourceInstanceBindingSite extends AssertionBindingSite {
    
    private Resource resourceInstance;

    
    public Resource getResourceInstance() {
        return resourceInstance;
    }

    
    public void setResourceInstance(Resource resourceInstance) {
        this.resourceInstance = resourceInstance;
    }
    
    
}
