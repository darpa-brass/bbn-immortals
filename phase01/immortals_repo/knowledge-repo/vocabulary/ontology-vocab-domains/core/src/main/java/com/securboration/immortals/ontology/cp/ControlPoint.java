package com.securboration.immortals.ontology.cp;

import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.lang.CompiledCodeUnit;

/**
 * A control point is a location where one of several DFUs that conform to some
 * functional signature can be inserted.
 * 
 * Although the name implies a single "point" in code, in reality the various
 * functional aspects of control point can be distributed across several
 * methods. For example, lines of code in an init() method might instantiate the
 * DFU whereas lines in a process() method would actually use it.
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A control point is a location where one of several DFUs that conform" +
    " to some functional signature can be inserted.  Although the name" +
    " implies a single \"point\" in code, in reality the various functional" +
    " aspects of control point can be distributed across several methods." +
    " For example, lines of code in an init() method might instantiate the" +
    " DFU whereas lines in a process() method would actually use it. " +
    " @author jstaples ")
public class ControlPoint {

    /**
     * The class containing the control point
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The class containing the control point")
    private CompiledCodeUnit owner;

    /**
     * Describes the functional signature of a DFU that should be injected here
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Describes the functional signature of a DFU that should be" +
        " injected here")
    private Class<? extends Functionality> functionalSignature;

    /**
     * Uniquely identifies the control point
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Uniquely identifies the control point")
    private String controlPointUuid;

    public CompiledCodeUnit getOwner() {
        return owner;
    }

    public void setOwner(CompiledCodeUnit owner) {
        this.owner = owner;
    }

    public String getControlPointUuid() {
        return controlPointUuid;
    }

    public void setControlPointUuid(String controlPointUuid) {
        this.controlPointUuid = controlPointUuid;
    }

    
    public Class<? extends Functionality> getFunctionalSignature() {
        return functionalSignature;
    }

    
    public void setFunctionalSignature(
            Class<? extends Functionality> functionalSignature) {
        this.functionalSignature = functionalSignature;
    }

}
