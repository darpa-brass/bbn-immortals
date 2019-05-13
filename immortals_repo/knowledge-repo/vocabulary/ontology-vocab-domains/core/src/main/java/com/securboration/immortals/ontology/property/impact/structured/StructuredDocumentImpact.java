package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.resources.xml.StructuredDocument;

public class StructuredDocumentImpact extends DataImpact {

    private Class<? extends StructuredDocument> impactedData;

    private StructuredDocumentImpactType impactType;

    private ResourceImpact applicableResource;

    public ResourceImpact getApplicableResource() {
        return applicableResource;
    }

    public void setApplicableResource(ResourceImpact applicableResource) {
        this.applicableResource = applicableResource;
    }

    public Class<? extends StructuredDocument> getImpactedData() {
        return impactedData;
    }

    public void setImpactedData(Class<? extends StructuredDocument> impactedData) {
        this.impactedData = impactedData;
    }

    public StructuredDocumentImpactType getImpactType() {
        return impactType;
    }

    public void setImpactType(StructuredDocumentImpactType impactType) {
        this.impactType = impactType;
    }
}
