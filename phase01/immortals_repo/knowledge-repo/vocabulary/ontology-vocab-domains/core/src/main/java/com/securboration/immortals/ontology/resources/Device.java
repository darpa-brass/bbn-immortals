package com.securboration.immortals.ontology.resources;

import com.securboration.immortals.ontology.core.Resource;

/**
 * A device
 * 
 * @author Securboration
 *
 */
public class Device extends Resource {
    
    private PlatformResource[] resources;

    
    public PlatformResource[] getResources() {
        return resources;
    }

    
    public void setResources(PlatformResource[] resources) {
        this.resources = resources;
    }

}
