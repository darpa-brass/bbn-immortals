package com.securboration.immortals.ontology.cp;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.property.impact.ProscriptiveCauseEffectAssertion;

public class RevisedGmeInterchangeFormat {

    /**
     * The constraints present in this GmeInterchangeFormat
     */
    private ProscriptiveCauseEffectAssertion[] constraints;
    
    /**
     * Describes the sea of resources available for performing that intent
     */
    private Resource[] availableResources;

    public Resource[] getAvailableResources() {
        return availableResources;
    }

    public void setAvailableResources(Resource[] availableResources) {
        this.availableResources = availableResources;
    }

    public ProscriptiveCauseEffectAssertion[] getConstraints() {
        return constraints;
    }

    public void setConstraints(ProscriptiveCauseEffectAssertion[] constraints) {
        this.constraints = constraints;
    }
}
