package com.securboration.immortals.ontology.expression.variable;

import com.securboration.immortals.ontology.assertion.binding.BindingSiteEcosystemResourceBase;
import com.securboration.immortals.ontology.metrics.MeasurementType;

/**
 * Describes a linkage to a measurement 
 * 
 * @author jstaples
 *
 */
public class MeasurementLinkage {
    
    /**
     * The type of measurement from which the variable's value can be derived
     */
    private Class<? extends MeasurementType> measurementType;
    
    /**
     * Specifies the type of resource that was measured to produce the indicated
     * measurement type. This can be either an abstract resource (in which case
     * any measurement made against the indicated abstract resource type
     * applies) or a specific resource instance (in which case only measurements
     * of that instance apply).
     */
    private BindingSiteEcosystemResourceBase measuredResource;

}
