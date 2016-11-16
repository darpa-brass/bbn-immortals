package com.securboration.immortals.ontology.cp;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.constraint.ValueCriterionType;
import com.securboration.immortals.ontology.metrics.Metric;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "A specification for the behavior of software in-mission " +
        "(ie, its intent with regard to mission objectives)"
        )
    )
public class MissionSpec extends SoftwareSpec {
    
    private String humanReadableForm;
    
    private ValueCriterionType assertionCriterion;
    
    private Metric rightValue;

    
    public String getHumanReadableForm() {
        return humanReadableForm;
    }

    
    public void setHumanReadableForm(String humanReadableForm) {
        this.humanReadableForm = humanReadableForm;
    }

    
    public ValueCriterionType getAssertionCriterion() {
        return assertionCriterion;
    }

    
    public void setAssertionCriterion(ValueCriterionType assertionCriterion) {
        this.assertionCriterion = assertionCriterion;
    }

    
    public Metric getRightValue() {
        return rightValue;
    }

    
    public void setRightValue(Metric rightValue) {
        this.rightValue = rightValue;
    }

}
