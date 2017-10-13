package com.securboration.immortals.ontology.deployment;

/**
 * A deployment model
 * @author Securboration
 *
 */
public class Deployment extends Dependency{
    
    /**
     * The deployment spec
     */
    private DeploymentSpec deploymentSpec;
    
    /**
     * The source being deployed
     */
    private DeploymentArtifact src;
    
    /**
     * The device on which the source is deployed
     */
    private DeploymentTarget dst;

}
