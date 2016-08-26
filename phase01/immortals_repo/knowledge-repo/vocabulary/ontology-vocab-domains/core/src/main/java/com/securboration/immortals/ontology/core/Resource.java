package com.securboration.immortals.ontology.core;

import com.securboration.immortals.ontology.property.Property;

/**
 * A description of something upon which software might depend
 * 
 * E.g., a library (code dependency)
 *       a platform architecture (embedded linux, windows, ...)
 *       compute resources (CPU, GPU, ...)
 *       an external resource accessed via IO (network, disk, ...)
 * 
 * @author Securboration
 *
 */
public class Resource {
    
    private String humanReadableDescription;
    
    private Property[] resourceProperty;

    
    public Property[] getResourceProperty() {
        return resourceProperty;
    }

    
    public void setResourceProperty(Property[] resourceProperty) {
        this.resourceProperty = resourceProperty;
    }


    
    public String getHumanReadableDescription() {
        return humanReadableDescription;
    }


    
    public void setHumanReadableDescription(String humanReadableDescription) {
        this.humanReadableDescription = humanReadableDescription;
    }

}
