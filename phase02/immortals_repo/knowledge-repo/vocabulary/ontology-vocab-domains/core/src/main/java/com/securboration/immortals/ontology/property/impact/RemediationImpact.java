package com.securboration.immortals.ontology.property.impact;

/**
 * The impact of a remediation strategy
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "The impact of a remediation strategy  @author jstaples ")
public class RemediationImpact extends ImpactStatement {
    
    /**
     * A predictive assertion that represents the remediation strategy
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A predictive assertion that represents the remediation strategy")
    private PredictiveCauseEffectAssertion remediationStrategy;

    
    public PredictiveCauseEffectAssertion getRemediationStrategy() {
        return remediationStrategy;
    }

    
    public void setRemediationStrategy(
            PredictiveCauseEffectAssertion remediationStrategy) {
        this.remediationStrategy = remediationStrategy;
    }

}
