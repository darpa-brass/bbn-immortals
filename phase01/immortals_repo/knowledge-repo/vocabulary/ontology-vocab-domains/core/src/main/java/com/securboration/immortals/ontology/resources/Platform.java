package com.securboration.immortals.ontology.resources;

import com.securboration.immortals.ontology.core.Resource;

/**
 * A platform is a type of resource on which software executes.  E.g., a server
 * or android device.
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A platform is a type of resource on which software executes.  E.g., a" +
    " server or android device.  @author jstaples ")
public class Platform extends Resource {
    
    /**
     * The resources available on the platform
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The resources available on the platform")
    private PlatformResource[] platformResources;

    
    public PlatformResource[] getPlatformResources() {
        return platformResources;
    }

    
    public void setPlatformResources(PlatformResource[] platformResources) {
        this.platformResources = platformResources;
    }

}
