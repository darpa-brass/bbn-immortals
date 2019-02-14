package com.securboration.immortals.ontology.resources.logical;

/**
 * The type of operating system
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "The type of operating system  @author jstaples ")
public class OsType {

    
    /**
     * The name of the OS.  E.g., "MacOSX"
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The name of the OS.  E.g., \"MacOSX\"")
    private String osName;
    
    /**
     * The version tag of the platform on which the OS runs.  E.g., "El Capitan"
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The version tag of the platform on which the OS runs.  E.g., \"El" +
        " Capitan\"")
    private String osVersionTag;

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersionTag() {
        return osVersionTag;
    }

    public void setOsVersionTag(String osVersionTag) {
        this.osVersionTag = osVersionTag;
    }
    
    
}
