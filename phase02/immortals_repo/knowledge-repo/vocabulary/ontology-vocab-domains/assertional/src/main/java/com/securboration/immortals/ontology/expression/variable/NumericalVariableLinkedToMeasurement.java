package com.securboration.immortals.ontology.expression.variable;

/**
 * A variable whose value is unknown but can be derived from a measurement of
 * the indicated type
 * 
 * @author jstaples
 *
 */
public class NumericalVariableLinkedToMeasurement extends NumericalVariable {
    
    /**
     * Describes a linkage of a measurement to a variable that can be used in an
     * expression
     */
    private MeasurementLinkage measurementLinkage;
    
}
