package com.securboration.immortals.bridge;

/**
 * A POJO describing a configuration we expect SwRI to define
 * 
 * @author jstaples
 *
 */
public class BridgePojo {
    
    /**
     * One of "V0_8_17", "V0_8_19". The version of MDL used by the client and
     * datasource.
     */
    private String initialMdlVersion;
    
    /**
     * One of "V0_8_17", "V0_8_19". The version of MDL used by the server.
     */
    private String updatedMdlVersion;
    
    /**
     * Arbitrary definition of an MDL schema. The version of MDL used by the
     * server.
     */
    private String updatedMdlSchema;
    
    
    public BridgePojo(){
        
    }


    
    public String getInitialMdlVersion() {
        return initialMdlVersion;
    }


    
    public void setInitialMdlVersion(String initialMdlVersion) {
        this.initialMdlVersion = initialMdlVersion;
    }


    
    public String getUpdatedMdlVersion() {
        return updatedMdlVersion;
    }


    
    public void setUpdatedMdlVersion(String updatedMdlVersion) {
        this.updatedMdlVersion = updatedMdlVersion;
    }


    
    public String getUpdatedMdlSchema() {
        return updatedMdlSchema;
    }


    
    public void setUpdatedMdlSchema(String updatedMdlSchema) {
        this.updatedMdlSchema = updatedMdlSchema;
    }

}
