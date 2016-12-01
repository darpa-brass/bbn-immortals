package com.securboration.immortals.ontology.resources;

/**
 * A device
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A device  @author jstaples ")
public class Software extends PlatformResource {
    
    private String applicationName;

    
    public String getApplicationName() {
        return applicationName;
    }

    
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

}
