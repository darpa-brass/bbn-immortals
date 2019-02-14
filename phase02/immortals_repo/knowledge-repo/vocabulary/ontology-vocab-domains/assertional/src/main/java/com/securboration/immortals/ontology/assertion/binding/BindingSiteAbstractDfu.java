package com.securboration.immortals.ontology.assertion.binding;

import com.securboration.immortals.ontology.functionality.Functionality;

/**
 * A binding site that is an abstract functionality performed by a DFU. I.e., a
 * corresponding assertion should bind to all DFUs implementing that
 * functionality.
 * 
 * @author jstaples
 *
 */
public class BindingSiteAbstractDfu extends BindingSiteBase {

    /**
     * The functionality performed by a DFU to which an assertion should bind
     */
    private Class<? extends Functionality> assertionalBindingSite;
    
}
