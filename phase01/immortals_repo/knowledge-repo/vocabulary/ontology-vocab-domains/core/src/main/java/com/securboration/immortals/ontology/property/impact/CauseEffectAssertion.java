package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.functionality.datatype.DataType;

/**
 * Models cause-effect relationships as a criterion attached to zero or more 
 * impact specifications
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Models cause-effect relationships as a criterion attached to zero or" +
    " more  impact specifications  @author jstaples ")
public class CauseEffectAssertion {
    
    /**
     * Human readable description of the cause-effect assertion
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Human readable description of the cause-effect assertion")
    private String humanReadableDescription;
    
    /**
     * The site to which the assertion binds
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The site to which the assertion binds")
    private AssertionBindingSite assertionBindingSite;
    
    /**
     * The applicable data type to which the assertion binds
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The applicable data type to which the assertion binds")
    private Class<? extends DataType> applicableDataType;
    
    /**
     * The criterion for this assertion to be valid
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The criterion for this assertion to be valid")
    private CriterionStatement criterion;
    
    /**
     * The impact of the criterion being met
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The impact of the criterion being met")
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
