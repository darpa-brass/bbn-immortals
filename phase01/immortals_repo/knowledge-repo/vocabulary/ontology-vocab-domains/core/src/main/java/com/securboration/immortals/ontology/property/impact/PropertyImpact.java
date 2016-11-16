package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.constraint.PropertyImpactType;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Models an impact on a property"
        )
    )
public class PropertyImpact extends ImpactStatement {
    
    private PropertyImpactType impactOnProperty;
    private Class<? extends Property> impactedProperty;
    
    public PropertyImpactType getImpactOnProperty() {
        return impactOnProperty;
    }
    
    public void setImpactOnProperty(PropertyImpactType impactOnProperty) {
        this.impactOnProperty = impactOnProperty;
    }
    
    public Class<? extends Property> getImpactedProperty() {
        return impactedProperty;
    }
    
    public void setImpactedProperty(Class<? extends Property> impactedProperty) {
        this.impactedProperty = impactedProperty;
    }

}
