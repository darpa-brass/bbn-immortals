package com.securboration.immortals.ontology.resource.containment;

import com.securboration.immortals.ontology.core.Resource;

public class ConcreteResourceNode extends ResourceContainmentModelNode{
    
    private Resource resource;

    
    public Resource getResource() {
        return resource;
    }

    
    public void setResource(Resource resource) {
        this.resource = resource;
    }

}
