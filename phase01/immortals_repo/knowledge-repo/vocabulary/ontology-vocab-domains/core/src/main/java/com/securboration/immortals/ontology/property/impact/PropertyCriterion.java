package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.constraint.PropertyCriterionType;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Models a change to a property value"
        )
    )
public class PropertyCriterion extends CriterionStatement {

    private PropertyCriterionType criterion;
    private Property property;
    
    public PropertyCriterionType getCriterion() {
        return criterion;
    }
    
    public void setCriterion(PropertyCriterionType criterion) {
        this.criterion = criterion;
    }

    
    public Property getProperty() {
        return property;
    }

    
    public void setProperty(Property property) {
        this.property = property;
    }
    
    
}
