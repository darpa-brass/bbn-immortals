package com.securboration.immortals.ontology.assertion.binding;

import com.securboration.immortals.ontology.dfu.instance.DfuInstance;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;

/**
 * A binding site that corresponds to the invocation of a DFU's functional 
 * aspect.
 * 
 * @author jstaples
 *
 */
public class BindingSiteDfuLifecycleMethodInvocation extends BindingSiteBase {

    /**
     * The DFU to which an assertion should bind
     */
    private DfuInstance assertionalBindingSite;
    
    /**
     * The method whose invocation
     */
    private Class<? extends FunctionalAspect> invokedAspect;
    
}
