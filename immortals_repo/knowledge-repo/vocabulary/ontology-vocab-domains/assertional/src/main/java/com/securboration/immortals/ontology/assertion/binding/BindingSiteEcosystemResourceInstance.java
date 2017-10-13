package com.securboration.immortals.ontology.assertion.binding;

import com.securboration.immortals.ontology.core.Resource;

/**
 * A binding site that is a resource instance. I.e., a corresponding assertion
 * should bind to only one specific resource instance.
 * 
 * @author jstaples
 *
 */
public class BindingSiteEcosystemResourceInstance extends BindingSiteEcosystemResourceBase {
    
    /**
     * The instance of the resource for this binding site
     */
    private Resource assertionalBindingSite;
    
}
