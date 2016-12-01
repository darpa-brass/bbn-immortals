package com.securboration.immortals.ontology.algorithm;

/**
 * A standard for an algorithm.  E.g., FIPS-197 for AES.
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A standard for an algorithm.  E.g., FIPS-197 for AES.  @author" +
    " jstaples ")
public class AlgorithmStandardProperty extends AlgorithmConfigurationProperty {

    private String standardName;
    private String ownerOrganization;
    private String url;
    
    public AlgorithmStandardProperty(){}

    public String getStandardName() {
        return standardName;
    }

    public void setStandardName(String standardName) {
        this.standardName = standardName;
    }

    public String getOwnerOrganization() {
        return ownerOrganization;
    }

    public void setOwnerOrganization(String ownerOrganization) {
        this.ownerOrganization = ownerOrganization;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
}
