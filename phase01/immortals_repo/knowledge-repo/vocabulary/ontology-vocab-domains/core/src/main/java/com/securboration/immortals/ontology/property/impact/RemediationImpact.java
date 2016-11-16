package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "A remediation strategy"
        )
    )
public class RemediationImpact extends ImpactStatement {
    
    private PredictiveCauseEffectAssertion remediationStrategy;

    
    public PredictiveCauseEffectAssertion getRemediationStrategy() {
        return remediationStrategy;
    }

    
    public void setRemediationStrategy(
            PredictiveCauseEffectAssertion remediationStrategy) {
        this.remediationStrategy = remediationStrategy;
    }

}
