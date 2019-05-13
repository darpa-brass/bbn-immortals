package com.securboration.immortals.ontology.functionality;

import com.securboration.immortals.ontology.core.Resource;

public class AspectConfiguration {

    private Class<? extends Resource> requiredResource;

    private boolean optional;

    private Resource defaultResource;

    public Class<? extends Resource> getRequiredResource() {
        return requiredResource;
    }

    public void setRequiredResource(Class<? extends Resource> requiredResource) {
        this.requiredResource = requiredResource;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public Resource getDefaultResource() {
        return defaultResource;
    }

    public void setDefaultResource(Resource defaultResource) {
        this.defaultResource = defaultResource;
    }
}
