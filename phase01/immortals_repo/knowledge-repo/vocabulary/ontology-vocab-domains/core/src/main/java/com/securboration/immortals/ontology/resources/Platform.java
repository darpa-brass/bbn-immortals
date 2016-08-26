package com.securboration.immortals.ontology.resources;

import com.securboration.immortals.ontology.core.Resource;

public class Platform extends Resource {
    
    private PlatformResource[] platformResources;

    
    public PlatformResource[] getPlatformResources() {
        return platformResources;
    }

    
    public void setPlatformResources(PlatformResource[] platformResources) {
        this.platformResources = platformResources;
    }

}
