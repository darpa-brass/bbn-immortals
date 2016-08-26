package com.securboration.immortals.ontology.core;

import com.securboration.immortals.ontology.resources.ExecutionPlatform;

/**
 * Binds a software and platform abstraction to create an executable abstraction
 * 
 * @author Securboration
 *
 */
public class SoftwareDeployment extends Resource {

    /**
     * A model of the software to be executed
     */
    private SoftwareAnalysisModel software;

    /**
     * The platform on which the software will execute
     */
    private ExecutionPlatform platform;

    public SoftwareAnalysisModel getSoftware() {
        return software;
    }

    public void setSoftware(SoftwareAnalysisModel software) {
        this.software = software;
    }

    public ExecutionPlatform getPlatform() {
        return platform;
    }

    public void setPlatform(ExecutionPlatform platform) {
        this.platform = platform;
    }

}
