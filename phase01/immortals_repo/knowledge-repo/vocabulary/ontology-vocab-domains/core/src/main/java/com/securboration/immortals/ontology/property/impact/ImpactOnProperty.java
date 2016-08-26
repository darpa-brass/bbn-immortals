package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Models the impact of a property on another property"
        )
    )
public class ImpactOnProperty extends ImpactSpecification {
    
    private Class<? extends Property> impactedProperty;

    
    public Class<? extends Property> getImpactedProperty() {
        return impactedProperty;
    }

    
    public void setImpactedProperty(Class<? extends Property> impactedProperty) {
        this.impactedProperty = impactedProperty;
    }

}
