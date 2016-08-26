package com.securboration.immortals.ontology.functionality.dataproperties;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.constraint.BinaryComparisonOperatorType;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Describes the relationship between one fidelity and others"
        )
    )
public class QualitativeFidelityAssertion {
    
    private Class<? extends Fidelity> subjectOfAssertion;
    
    private BinaryComparisonOperatorType operator;
    
    private Class<? extends Fidelity>[] objectOfAssertion;
    
    public Class<? extends Fidelity> getSubjectOfAssertion() {
        return subjectOfAssertion;
    }
    
    public void setSubjectOfAssertion(
            Class<? extends Fidelity> subjectOfAssertion) {
        this.subjectOfAssertion = subjectOfAssertion;
    }
    
    public Class<? extends Fidelity>[] getObjectOfAssertion() {
        return objectOfAssertion;
    }
    
    public void setObjectOfAssertion(
            Class<? extends Fidelity>[] objectOfAssertion) {
        this.objectOfAssertion = objectOfAssertion;
    }

    
    public BinaryComparisonOperatorType getOperator() {
        return operator;
    }

    
    public void setOperator(BinaryComparisonOperatorType operator) {
        this.operator = operator;
    }
    
}
