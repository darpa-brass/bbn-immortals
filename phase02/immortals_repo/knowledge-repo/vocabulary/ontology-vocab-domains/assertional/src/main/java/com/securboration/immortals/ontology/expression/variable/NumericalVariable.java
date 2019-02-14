package com.securboration.immortals.ontology.expression.variable;


/**
 * 
 * @author jstaples
 *
 */
public class NumericalVariable extends ExpressionVariable {
    
    /**
     * The target (desired) unit in which the variable is expressed. For
     * example, a measurement of type "speed" might have been originally made in
     * unit "km/s" but we actually need unit "m/s" in the variable. The solution
     * is to look up a conversion factor from "km/s" to "m/s".
     */
    private String targetUnit;
    
    
    
}
