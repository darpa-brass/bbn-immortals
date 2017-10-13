package com.securboration.immortals.ontology.assertion;

import com.securboration.immortals.ontology.assertion.binding.BindingSiteBase;
import com.securboration.immortals.ontology.expression.BooleanExpression;

/**
 * An assertional statement
 * 
 * @author jstaples
 *
 */
public class CauseEffectAssertion {
    
    /**
     * The thing the assertion binds to. E.g., in a descriptive assertion the
     * object specifies what was observed; in a predictive assertion the object
     * specifies
     */
    private BindingSiteBase objectOfAssertion;
    
    /**
     * The preconditions that must be met for the assertion to be true
     */
    private BooleanExpression[] criterion;
    
}
