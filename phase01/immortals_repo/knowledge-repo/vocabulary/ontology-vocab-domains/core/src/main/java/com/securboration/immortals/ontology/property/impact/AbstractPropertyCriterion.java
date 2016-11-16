package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.constraint.PropertyCriterionType;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Models a change to a type of property"
        )
    )
public class AbstractPropertyCriterion extends CriterionStatement {

    private PropertyCriterionType criterion;
    private Class<? extends Property> property;
    
    public PropertyCriterionType getCriterion() {
        return criterion;
    }
    
    public void setCriterion(PropertyCriterionType criterion) {
        this.criterion = criterion;
    }
    
    public Class<? extends Property> getProperty() {
        return property;
    }
    
    public void setProperty(Class<? extends Property> property) {
        this.property = property;
    }
    
    
}
