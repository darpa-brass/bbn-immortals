package com.securboration.immortals.ontology.android;

import com.securboration.immortals.ontology.resources.ExecutionPlatform;
import com.securboration.immortals.ontology.resources.PlatformResource;

public class AndroidPlatform extends ExecutionPlatform {
    
    private String androidPlatformVersion;
    
    private PlatformResource[] platformResources;

    
    public String getAndroidPlatformVersion() {
        return androidPlatformVersion;
    }

    
    public void setAndroidPlatformVersion(String androidPlatformVersion) {
        this.androidPlatformVersion = androidPlatformVersion;
    }


    
    public PlatformResource[] getPlatformResources() {
        return platformResources;
    }


    
    public void setPlatformResources(PlatformResource[] platformResources) {
        this.platformResources = platformResources;
    }

}
