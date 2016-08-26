package com.securboration.immortals.ontology.core;

import com.securboration.immortals.ontology.resources.environment.OperatingEnvironment;
import com.securboration.immortals.ontology.resources.perturbation.ResourcePerturbation;

/**
 * Describes the deployment of one or more software products to one or more
 * devices in an environment
 * 
 * @author Securboration
 *
 */
public class DeploymentModel {

    /**
     * Describes the environment external to the platforms on which software
     * will be run. The environment will likely change after deployment and is
     * difficult to predict.
     */
    private OperatingEnvironment operatingEnvironment;

    /**
     * The mappings of software to be deployed to the platforms on which the
     * software will execute.
     */
    private SoftwareDeployment[] softwareDeployments;

    /**
     * The resource and environmental perturbations under which our software is
     * expected to remain functional. E.g., maybe our software is expected to
     * remain functional if the device runs out of local storage.
     */
    private ResourcePerturbation[] anticipatedPerturbations;

    public OperatingEnvironment getOperatingEnvironment() {
        return operatingEnvironment;
    }

    public void setOperatingEnvironment(OperatingEnvironment operatingEnvironment) {
        this.operatingEnvironment = operatingEnvironment;
    }

    public SoftwareDeployment[] getSoftwareDeployments() {
        return softwareDeployments;
    }

    public void setSoftwareDeployments(SoftwareDeployment[] softwareDeployments) {
        this.softwareDeployments = softwareDeployments;
    }

    public ResourcePerturbation[] getAnticipatedPerturbations() {
        return anticipatedPerturbations;
    }

    public void setAnticipatedPerturbations(
            ResourcePerturbation[] anticipatedPerturbations) {
        this.anticipatedPerturbations = anticipatedPerturbations;
    }
}
