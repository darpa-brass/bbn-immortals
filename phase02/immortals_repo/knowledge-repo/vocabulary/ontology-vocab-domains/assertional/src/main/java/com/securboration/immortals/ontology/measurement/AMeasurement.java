package com.securboration.immortals.ontology.measurement;

import com.securboration.immortals.ontology.expression.BooleanExpression;
import com.securboration.immortals.ontology.unit.UnitOfMeasure;

/**
 * A measurement
 * 
 * @author jstaples
 *
 */
public class AMeasurement {
    
    /**
     * A variable whose value led to the violation
     */
    private BooleanExpression problematicExpression;
    
    /**
     * The unit in which the measurement was made
     */
    private UnitOfMeasure unitOfMeasure;

}
