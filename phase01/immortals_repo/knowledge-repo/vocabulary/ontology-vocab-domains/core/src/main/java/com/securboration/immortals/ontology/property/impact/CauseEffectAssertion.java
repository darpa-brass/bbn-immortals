package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Models cause-effect relationships as a criterion attached to zero " +
        "or more impact specifications."
        )
    )
public class CauseEffectAssertion {
    
    private String humanReadableDescription;
    
    private AssertionBindingSite assertionBindingSite;
    private Class<? extends DataType> applicableDataType;
    private CriterionStatement criterion;
    private ImpactStatement[] impact;
    
    
    public String getHumanReadableDescription() {
        return humanReadableDescription;
    }

    
    public void setHumanReadableDescription(String humanReadableDescription) {
        this.humanReadableDescription = humanReadableDescription;
    }


    
    public CriterionStatement getCriterion() {
        return criterion;
    }


    
    public void setCriterion(CriterionStatement criterion) {
        this.criterion = criterion;
    }


    
    public ImpactStatement[] getImpact() {
        return impact;
    }


    
    public void setImpact(ImpactStatement[] impact) {
        this.impact = impact;
    }


    
    public Class<? extends DataType> getApplicableDataType() {
        return applicableDataType;
    }


    
    public void setApplicableDataType(
            Class<? extends DataType> applicableDataType) {
        this.applicableDataType = applicableDataType;
    }


    
    public AssertionBindingSite getAssertionBindingSite() {
        return assertionBindingSite;
    }


    
    public void setAssertionBindingSite(AssertionBindingSite assertionBindingSite) {
        this.assertionBindingSite = assertionBindingSite;
    }

}
