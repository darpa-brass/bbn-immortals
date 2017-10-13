package com.securboration.immortals.ontology.dfu;

/**
 * A dfu that allows us to specify which of several children that perform some
 * functionality should actually be used to achieve that functionality. We
 * should periodically monitor conditions and update this component with the
 * best component to use.
 * 
 * @author Securboration
 *
 */
public class DfuSelectionMechanismActive extends DfuCannedSelectable {

    /**
     * A DFU that allows us to specify the dfu that should be used
     */
    private Dfu selectionMechanism;

    /**
     * The DFUs to select between
     */
    private Dfu[] selectionOptions;

    public Dfu getSelectionMechanism() {
        return selectionMechanism;
    }

    public void setSelectionMechanism(Dfu selectionMechanism) {
        this.selectionMechanism = selectionMechanism;
    }

    public Dfu[] getSelectionOptions() {
        return selectionOptions;
    }

    public void setSelectionOptions(Dfu[] selectionOptions) {
        this.selectionOptions = selectionOptions;
    }

}
