package com.securboration.immortals.ontology.assertion.binding;

/**
 * A virtual binding site that allows multiple actual binding sites to be 
 * presented as one.
 * 
 * @author jstaples
 *
 */
public class BindingSiteAggregation extends BindingSiteBase {

    /**
     * The binding sites being aggregated
     */
    private BindingSiteBase bindingSite[];
    
}
