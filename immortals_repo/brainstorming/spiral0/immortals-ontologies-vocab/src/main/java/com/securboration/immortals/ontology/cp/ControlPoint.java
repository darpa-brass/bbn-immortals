package com.securboration.immortals.ontology.cp;

import com.securboration.immortals.ontology.bytecode.AClass;
import com.securboration.immortals.ontology.functionality.Functionality;

/**
 * A control point is a location where one of several DFUs that conform to some
 * functional signature can be inserted.
 * <p>
 * Although the name implies a single "point" in code, in reality the various
 * functional aspects of control point can be distributed across several
 * methods. For example, lines of code in an init() method might instantiate the
 * DFU whereas lines in a process() method would actually use it.
 * 
 * @author Securboration
 *
 */
public class ControlPoint {

    /**
     * The class containing the control point
     */
    private AClass ownerClass;

    /**
     * Describes the functional signature of a DFU that should be injected here
     */
    private Functionality functionalSignature;

    /**
     * Uniquely identifies the control point
     */
    private String controlPointUuid;

    public AClass getOwnerClass() {
        return ownerClass;
    }

    public void setOwnerClass(AClass ownerClass) {
        this.ownerClass = ownerClass;
    }

    public String getControlPointUuid() {
        return controlPointUuid;
    }

    public void setControlPointUuid(String controlPointUuid) {
        this.controlPointUuid = controlPointUuid;
    }

    public Functionality getFunctionalSignature() {
        return functionalSignature;
    }

    public void setFunctionalSignature(Functionality functionalSignature) {
        this.functionalSignature = functionalSignature;
    }

}
