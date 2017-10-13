package com.securboration.immortals.ontology.resources.constraints;

/**
 * An expression that contains a single variable
 * 
 * @author Securboration
 *
 */
public class ExpressionNumber extends Expression {
    
    /**
     * The value observed
     */
    private double value;

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
    
}
