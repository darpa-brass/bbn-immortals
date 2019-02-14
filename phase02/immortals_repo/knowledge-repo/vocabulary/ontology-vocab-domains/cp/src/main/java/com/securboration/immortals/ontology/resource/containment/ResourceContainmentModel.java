package com.securboration.immortals.ontology.resource.containment;

import com.securboration.immortals.ontology.core.HumanReadable;

public class ResourceContainmentModel implements HumanReadable{
    
    private String humanReadableDesc;
    
    private ResourceContainmentModelNode[] resourceModel;

    @Override
    public String getHumanReadableDesc() {
        return humanReadableDesc;
    }

    
    public ResourceContainmentModelNode[] getResourceModel() {
        return resourceModel;
    }

    
    public void setResourceModel(ResourceContainmentModelNode[] resourceModel) {
        this.resourceModel = resourceModel;
    }

    
    public void setHumanReadableDesc(String humanReadableDesc) {
        this.humanReadableDesc = humanReadableDesc;
    }

}
