package com.securboration.immortals.ontology.resource.containment;

import com.securboration.immortals.ontology.core.Resource;

public class AbstractResourceNode extends ResourceContainmentModelNode{
    
    private Class<? extends Resource> resourceType;

    
    public Class<? extends Resource> getResourceType() {
        return resourceType;
    }

    
    public void setResourceType(Class<? extends Resource> resourceType) {
        this.resourceType = resourceType;
    }

}
