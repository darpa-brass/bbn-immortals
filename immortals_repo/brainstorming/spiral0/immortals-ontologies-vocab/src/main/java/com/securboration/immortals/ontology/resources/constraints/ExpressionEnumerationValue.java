package com.securboration.immortals.ontology.resources.constraints;

/**
 * An expression that contains a single variable
 * 
 * @author Securboration
 *
 */
public class ExpressionEnumerationValue extends Expression {
    
    /**
     * The actual value observed
     */
    private String actualValue;
    
    /**
     * The possible values that could have been observed
     */
    private String[] possibleValues;

    public String getActualValue() {
        return actualValue;
    }

    public void setActualValue(String actualValue) {
        this.actualValue = actualValue;
    }

    public String[] getPossibleValues() {
        return possibleValues;
    }

    public void setPossibleValues(String[] possibleValues) {
        this.possibleValues = possibleValues;
    }
}
