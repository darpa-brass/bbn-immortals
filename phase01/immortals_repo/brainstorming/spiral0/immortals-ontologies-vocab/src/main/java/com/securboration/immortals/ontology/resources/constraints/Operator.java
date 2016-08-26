package com.securboration.immortals.ontology.resources.constraints;

/**
 * An operator for an expression
 * @author Securboration
 *
 */
public class Operator {

    /**
     * An enumeration of permitted operations, one of which is selected
     */
    private OperatorType operatorType;

    public OperatorType getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(OperatorType operatorType) {
        this.operatorType = operatorType;
    }
}
