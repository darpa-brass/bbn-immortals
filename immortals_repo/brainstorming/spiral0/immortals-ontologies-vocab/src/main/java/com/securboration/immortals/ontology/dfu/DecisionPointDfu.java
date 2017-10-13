package com.securboration.immortals.ontology.dfu;

/**
 * A DFU that decides which of various child DFUs to use to implement the
 * specified functionality
 * 
 * @author Securboration
 *
 */
public class DecisionPointDfu extends Dfu{
    
    /**
     * How do we swap between this and some other DFU?
     */
    private ContextSwitchingMechanism contextSwitchingMechanism;

    public ContextSwitchingMechanism getContextSwitchingMechanism() {
        return contextSwitchingMechanism;
    }

    public void setContextSwitchingMechanism(
            ContextSwitchingMechanism contextSwitchingMechanism) {
        this.contextSwitchingMechanism = contextSwitchingMechanism;
    }

}
