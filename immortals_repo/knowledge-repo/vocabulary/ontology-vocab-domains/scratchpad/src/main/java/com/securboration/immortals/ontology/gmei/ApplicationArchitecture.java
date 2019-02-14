package com.securboration.immortals.ontology.gmei;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.property.impact.CauseEffectAssertion;

public class ApplicationArchitecture {

    /**
     * The (flattened) sea of resources available for performing that intent
     */
    private Resource[] availableResources;

    private CauseEffectAssertion[] causeEffectAssertions;

    private FunctionalAspect[] functionalAspects;

    public Resource[] getAvailableResources() {
        return availableResources;
    }

    public void setAvailableResources(Resource[] availableResources) {
        this.availableResources = availableResources;
    }

    public CauseEffectAssertion[] getCauseEffectAssertions() {
        return causeEffectAssertions;
    }

    public void setCauseEffectAssertions(CauseEffectAssertion[] causeEffectAssertions) {
        this.causeEffectAssertions = causeEffectAssertions;
    }

    public FunctionalAspect[] getFunctionalAspects() {
        return functionalAspects;
    }

    public void setFunctionalAspects(FunctionalAspect[] functionalAspects) {
        this.functionalAspects = functionalAspects;
    }
}
