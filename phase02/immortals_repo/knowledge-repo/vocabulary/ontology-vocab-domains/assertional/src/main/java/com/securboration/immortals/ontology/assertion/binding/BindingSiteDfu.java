package com.securboration.immortals.ontology.assertion.binding;

import com.securboration.immortals.ontology.dfu.instance.DfuInstance;

/**
 * A binding site that is a specific instance of a DFU (an actual code unit that
 * implements some functionality).
 * 
 * @author jstaples
 *
 */
public class BindingSiteDfu extends BindingSiteBase {

    /**
     * The DFU to which an assertion should bind
     */
    private DfuInstance assertionalBindingSite;
    
}
