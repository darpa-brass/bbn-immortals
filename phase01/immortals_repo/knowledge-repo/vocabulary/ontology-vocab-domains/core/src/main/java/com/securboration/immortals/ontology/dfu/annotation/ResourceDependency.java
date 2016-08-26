package com.securboration.immortals.ontology.dfu.annotation;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.property.Property;

public class ResourceDependency {

    public Class<? extends Resource> resourceType;
    
    public Property[] associationProperties;
    
    public Class<? extends Resource> getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(Class<? extends Resource> resourceType) {
        this.resourceType = resourceType;
    }
    
    public Property[] getAssociationProperties() {
        return associationProperties;
    }
    
    public void setAssociationProperties(Property[] associationProperties) {
        this.associationProperties = associationProperties;
    }
    
}
