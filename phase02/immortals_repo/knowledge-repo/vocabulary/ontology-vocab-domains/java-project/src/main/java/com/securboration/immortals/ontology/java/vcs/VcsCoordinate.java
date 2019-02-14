package com.securboration.immortals.ontology.java.vcs;


/**
 * A coordinate that uniquely identifies a file in a Version Control System
 * (VCS)
 * 
 * @author jstaples
 *
 */
public class VcsCoordinate {
    
    /**
     * A URL at which the resource can be retrieved
     */
    private String versionControlUrl;
    
    /**
     * The version of the resource
     */
    private String version;

    
    public String getVersionControlUrl() {
        return versionControlUrl;
    }

    
    public void setVersionControlUrl(String versionControlUrl) {
        this.versionControlUrl = versionControlUrl;
    }

    
    public String getVersion() {
        return version;
    }

    
    public void setVersion(String version) {
        this.version = version;
    }

}
