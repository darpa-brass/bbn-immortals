package com.securboration.immortals.ontology.dfu;

import com.securboration.immortals.ontology.functionality.datatype.DataType;

/**
 * Describes how to context switch from one DFU to another
 * 
 * @author Securboration
 *
 */
public class ContextSwitchingMechanism {

    /**
     * The state of the DFU that must be carried across a context switch
     */
    private DataType state;

    /**
     * A DFU that allows us to acquire state before context switching
     */
    private Dfu acquireState;

    /**
     * A DFU that allows us to set the state before context switching
     */
    private Dfu restoreState;

    /**
     * How we can inject the DFU
     */
    private DfuInjectionMechanism injectionMechanism;

    public DataType getState() {
        return state;
    }

    public void setState(DataType state) {
        this.state = state;
    }

    public Dfu getAcquireState() {
        return acquireState;
    }

    public void setAcquireState(Dfu acquireState) {
        this.acquireState = acquireState;
    }

    public Dfu getRestoreState() {
        return restoreState;
    }

    public void setRestoreState(Dfu restoreState) {
        this.restoreState = restoreState;
    }

    public DfuInjectionMechanism getInjectionMechanism() {
        return injectionMechanism;
    }

    public void setInjectionMechanism(DfuInjectionMechanism injectionMechanism) {
        this.injectionMechanism = injectionMechanism;
    }

}
