package com.securboration.immortals.ontology.dfu;

/**
 * Describes a dfu that selects the best of a set of DFUs that perform some
 * functionality based on current conditions. We should periodically update this
 * component with current observations.
 * 
 * @author Securboration
 *
 */
public class DfuSelectionMechanismPassive extends DfuCannedSelectable{

    /**
     * A DFU that allows us to inject the current environmental conditions and
     * will pick the best dfu on our behalf
     */
    private Dfu selectionMechanism;

    public Dfu getSelectionMechanism() {
        return selectionMechanism;
    }

    public void setSelectionMechanism(Dfu selectionMechanism) {
        this.selectionMechanism = selectionMechanism;
    }

}
