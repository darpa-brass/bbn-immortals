package com.securboration.immortals.ontology.constraint;

import com.securboration.immortals.ontology.property.impact.CriterionStatement;

public class DataflowConstraintViolation extends DataflowViolation {

    private RestorativeAspectInstances restorativeAspectInstances;

    private CriterionStatement criterionViolated;

    public RestorativeAspectInstances getRestorativeAspectInstances() {
        return restorativeAspectInstances;
    }

    public void setRestorativeAspectInstances(RestorativeAspectInstances restorativeAspectInstances) {
        this.restorativeAspectInstances = restorativeAspectInstances;
    }

    public CriterionStatement getCriterionViolated() {
        return criterionViolated;
    }

    public void setCriterionViolated(CriterionStatement criterionViolated) {
        this.criterionViolated = criterionViolated;
    }
}
