package com.securboration.immortals.ontology.constraint;

import com.securboration.immortals.ontology.analysis.DataflowGraphComponent;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.property.impact.AnalysisImpact;

public class InjectionImpact extends AnalysisImpact {

    private DataflowGraphComponent[] dataflows;

    private Class<? extends FunctionalAspect> aspectImplemented;

    public DataflowGraphComponent[] getDataflows() {
        return dataflows;
    }

    public void setDataflows(DataflowGraphComponent[] dataflows) {
        this.dataflows = dataflows;
    }

    public Class<? extends FunctionalAspect> getAspectImplemented() {
        return aspectImplemented;
    }

    public void setAspectImplemented(Class<? extends FunctionalAspect> aspectImplemented) {
        this.aspectImplemented = aspectImplemented;
    }
}
