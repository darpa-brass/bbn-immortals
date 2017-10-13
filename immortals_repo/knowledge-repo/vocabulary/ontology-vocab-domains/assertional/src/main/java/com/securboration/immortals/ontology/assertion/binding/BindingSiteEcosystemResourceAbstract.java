package com.securboration.immortals.ontology.assertion.binding;

import com.securboration.immortals.ontology.core.Resource;

/**
 * A binding site that is an abstract resource type. I.e., a corresponding
 * assertion should bind to all concrete instances of that resource type.
 * 
 * @author jstaples
 *
 */
public class BindingSiteEcosystemResourceAbstract extends BindingSiteEcosystemResourceBase {

    /**
     * The type of resource to which an assertion should bind
     */
    private Class<? extends Resource> assertionalBindingSite;
    
    
    
}
