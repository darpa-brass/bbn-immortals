package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.constraint.ResourceCriterionType;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Models a change to a resource"
        )
    )
public class ResourceCriterion extends CriterionStatement {

    private ResourceCriterionType criterion;
    private Class<? extends Resource> property;
    
    public ResourceCriterionType getCriterion() {
        return criterion;
    }
    
    public void setCriterion(ResourceCriterionType criterion) {
        this.criterion = criterion;
    }

    
    public Class<? extends Resource> getProperty() {
        return property;
    }

    
    public void setProperty(Class<? extends Resource> property) {
        this.property = property;
    }
    
    
}
