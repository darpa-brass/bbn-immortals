package com.securboration.immortals.ontology.resources;

import com.securboration.immortals.ontology.core.Resource;

public class ResourceMigrationTarget {
    
    private Resource targetResource;
    
    private String rationale;

    public Resource getTargetResource() {
        return targetResource;
    }

    public void setTargetResource(Resource targetResource) {
        this.targetResource = targetResource;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }
}
