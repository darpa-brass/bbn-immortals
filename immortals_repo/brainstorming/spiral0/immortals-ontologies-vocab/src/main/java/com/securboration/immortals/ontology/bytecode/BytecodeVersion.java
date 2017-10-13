package com.securboration.immortals.ontology.bytecode;

/**
 * A model of the bytecode versioning scheme
 * 
 * @author Securboration
 *
 */
public class BytecodeVersion { 
    
    /**
     * The major version
     * 
     * E.g., 45
     */
    private String majorVersionTag;
    
    /**
     * The minor version
     * 
     * E.g., 3
     */
    private String minorVersionTag;
    
    /**
     * The platform version
     * 
     * E.g., 1.8
     */
    private String platformVersionTag;

    public String getMajorVersionTag() {
        return majorVersionTag;
    }

    public void setMajorVersionTag(String majorVersionTag) {
        this.majorVersionTag = majorVersionTag;
    }

    public String getMinorVersionTag() {
        return minorVersionTag;
    }

    public void setMinorVersionTag(String minorVersionTag) {
        this.minorVersionTag = minorVersionTag;
    }

    public String getPlatformVersionTag() {
        return platformVersionTag;
    }

    public void setPlatformVersionTag(String platformVersionTag) {
        this.platformVersionTag = platformVersionTag;
    }

}
