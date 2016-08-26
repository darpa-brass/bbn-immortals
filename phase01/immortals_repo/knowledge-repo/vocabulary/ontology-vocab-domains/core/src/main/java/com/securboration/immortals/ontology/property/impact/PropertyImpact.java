package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.constraint.PropertyCriterionType;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Models the impact of a change to a property"
        )
    )
public class PropertyImpact {
    
    private String humanReadableDescription;
    private Class<? extends Property> property;
    private PropertyCriterionType criterionForImpact;
    private ImpactSpecification[] impact;
    
    public String getHumanReadableDescription() {
        return humanReadableDescription;
    }
    
    public void setHumanReadableDescription(String humanReadableDescription) {
        this.humanReadableDescription = humanReadableDescription;
    }
    
    public Class<? extends Property> getProperty() {
        return property;
    }
    
    public void setProperty(Class<? extends Property> property) {
        this.property = property;
    }
    
    public PropertyCriterionType getCriterionForImpact() {
        return criterionForImpact;
    }
    
    public void setCriterionForImpact(PropertyCriterionType criterionForImpact) {
        this.criterionForImpact = criterionForImpact;
    }
    
    public ImpactSpecification[] getImpact() {
        return impact;
    }
    
    public void setImpact(ImpactSpecification[] impact) {
        this.impact = impact;
    }
}
