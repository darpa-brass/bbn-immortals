package com.securboration.immortals.ontology.dfu;

/**
 * An abstraction that binds what is being done to how it is implemented in
 * bytecode
 * 
 * @author Securboration
 *
 */
public class DfuContextSwitchable extends Dfu{
    
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
