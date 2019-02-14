package com.securboration.immortals.ontology.profiling;

/**
 * Information about a platform. E.g., may describe special configurations
 * useful for differentiating between gathered results.
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Information about a platform. E.g., may describe special" +
    " configurations useful for differentiating between gathered results. " +
    " @author jstaples ")
public class TestPlatformInfo {

    /**
     * Human readable information about the platform
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Human readable information about the platform")
    private String platformInfo;

    
    public String getPlatformInfo() {
        return platformInfo;
    }

    
    public void setPlatformInfo(String platformInfo) {
        this.platformInfo = platformInfo;
    }
    
}
