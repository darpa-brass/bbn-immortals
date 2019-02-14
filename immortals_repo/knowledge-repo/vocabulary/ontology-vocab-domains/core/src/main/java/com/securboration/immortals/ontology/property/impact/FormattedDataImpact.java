package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.resources.FormattedData;

public class FormattedDataImpact extends DataImpact {

    private Class<? extends FormattedData> impactedData;

    private FormattedDataImpactType impactType;

    private ResourceImpact applicableResource;

    public Class<? extends FormattedData> getImpactedData() {
        return impactedData;
    }

    public void setImpactedData(Class<? extends FormattedData> impactedData) {
        this.impactedData = impactedData;
    }

    public FormattedDataImpactType getImpactType() {
        return impactType;
    }

    public void setImpactType(FormattedDataImpactType impactType) {
        this.impactType = impactType;
    }

    public ResourceImpact getApplicableResource() {
        return applicableResource;
    }

    public void setApplicableResource(ResourceImpact applicableResource) {
        this.applicableResource = applicableResource;
    }
}
