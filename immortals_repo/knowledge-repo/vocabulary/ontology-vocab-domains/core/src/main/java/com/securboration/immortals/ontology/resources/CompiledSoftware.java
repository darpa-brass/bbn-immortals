package com.securboration.immortals.ontology.resources;

/**
 * A software library that was compiled from source code
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A software library that was compiled from source code"
    )
public class CompiledSoftware extends Software {
    
    private String versionControlUrl;
    
    private String softwareCoordinate;

    
    public String getVersionControlUrl() {
        return versionControlUrl;
    }

    
    public void setVersionControlUrl(String versionControlUrl) {
        this.versionControlUrl = versionControlUrl;
    }

    
    public String getSoftwareCoordinate() {
        return softwareCoordinate;
    }

    
    public void setSoftwareCoordinate(String softwareCoordinate) {
        this.softwareCoordinate = softwareCoordinate;
    }

}
