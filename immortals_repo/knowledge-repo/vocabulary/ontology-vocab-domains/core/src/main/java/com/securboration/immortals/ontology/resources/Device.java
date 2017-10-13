package com.securboration.immortals.ontology.resources;

import com.securboration.immortals.ontology.core.Resource;

/**
 * An abstract device
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "An abstract device  @author jstaples ")
public class Device extends Resource {
    
    /**
     * The platform resources connected to a device
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The platform resources connected to a device")
    private PlatformResource[] resources;

    
    public PlatformResource[] getResources() {
        return resources;
    }

    
    public void setResources(PlatformResource[] resources) {
        this.resources = resources;
    }

}
