package com.securboration.immortals.ontology.expression.variable;


/**
 * A logical variable whose value is unknown
 * 
 * @author jstaples
 *
 */
public class LogicalVariableLinkedToMeasurement extends LogicalVariable {
    
    /**
     * Describes a measurement from which this variable can be derived
     */
    private MeasurementLinkage measurementLinkage;
    
    
}
