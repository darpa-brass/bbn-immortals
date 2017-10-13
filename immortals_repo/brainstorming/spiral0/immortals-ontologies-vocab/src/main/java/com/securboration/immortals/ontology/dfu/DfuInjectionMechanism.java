package com.securboration.immortals.ontology.dfu;

/**
 * Describes a method we call to inject this dfu
 * 
 * @author Securboration
 *
 */
public class DfuInjectionMechanism {

    /**
     * the state injection mechanism
     */
    private Dfu injectState;

    public Dfu getInjectState() {
        return injectState;
    }

    public void setInjectState(Dfu injectState) {
        this.injectState = injectState;
    }

}
