package com.securboration.immortals.ontology.assertion.types.predictive;

import com.securboration.immortals.ontology.assertion.AssertionalStatement;
import com.securboration.immortals.ontology.change.DeltaCriterionStatement;

/**
 * A predictive assertional statement describes the hypothesized impact of a 
 * change in one binding site on another.
 * 
 * @author jstaples
 *
 */
public class PredictiveAssertionalStatement extends AssertionalStatement {
    
    
    public Class<? extends DeltaCriterionStatement> deltaCondition;
    
}
