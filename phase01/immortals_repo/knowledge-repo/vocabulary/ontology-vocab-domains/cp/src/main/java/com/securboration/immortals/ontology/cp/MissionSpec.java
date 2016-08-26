package com.securboration.immortals.ontology.cp;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.constraint.ValueCriterionType;
import com.securboration.immortals.ontology.metrics.MeasuredValue;
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
    
    private ValueCriterionType valueCriterion;
    
    private MeasuredValue rightValue;

    
    public ValueCriterionType getValueCriterion() {
        return valueCriterion;
    }

    
    public void setValueCriterion(ValueCriterionType operator) {
        this.valueCriterion = operator;
    }

    
    public MeasuredValue getRightValue() {
        return rightValue;
    }

    
    public void setRightValue(MeasuredValue rightValue) {
        this.rightValue = rightValue;
    }


    
    public String getHumanReadableForm() {
        return humanReadableForm;
    }


    
    public void setHumanReadableForm(String humanReadableForm) {
        this.humanReadableForm = humanReadableForm;
    }

}
