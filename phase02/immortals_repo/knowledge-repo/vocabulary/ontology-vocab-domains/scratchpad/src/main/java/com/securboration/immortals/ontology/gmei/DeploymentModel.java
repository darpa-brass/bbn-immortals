package com.securboration.immortals.ontology.gmei;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.cp.FunctionalitySpec;
import com.securboration.immortals.ontology.property.impact.CauseEffectAssertion;
import com.securboration.immortals.ontology.resource.containment.ResourceContainmentModel;
import com.securboration.immortals.ontology.resources.ResourceMigrationTarget;

public class DeploymentModel {

    /**
     * Identifies a GME session
     */
    private String sessionIdentifier;

    /**
     * A human readable description of this interchange document
     */
    private String humanReadableDescription;

    /**
     * Describes the functionalities being performed by the software (ie, its
     * intent with regard to functionality)
     */
    private FunctionalitySpec[] functionalitySpec;
    
    /**
     * The (flattened) sea of resources available for performing that intent
     */
    private Resource[] availableResources;
    
    /**
     * Describes a recursive containment structure
     */
    private ResourceContainmentModel resourceContainmentModel;
    
    private CauseEffectAssertion[] causeEffectAssertions;
    
    private ResourceMigrationTarget[] resourceMigrationTargets;

    public CauseEffectAssertion[] getCauseEffectAssertions() {
        return causeEffectAssertions;
    }

    public void setCauseEffectAssertions(CauseEffectAssertion[] causeEffectAssertions) {
        this.causeEffectAssertions = causeEffectAssertions;
    }

    public Resource[] getAvailableResources() {
        return availableResources;
    }

    public void setAvailableResources(Resource[] availableResources) {
        this.availableResources = availableResources;
    }

    public String getSessionIdentifier() {
        return sessionIdentifier;
    }

    public void setSessionIdentifier(String sessionIdentifier) {
        this.sessionIdentifier = sessionIdentifier;
    }

    public FunctionalitySpec[] getFunctionalitySpec() {
        return functionalitySpec;
    }

    public void setFunctionalitySpec(FunctionalitySpec[] functionalitySpec) {
        this.functionalitySpec = functionalitySpec;
    }

    
    public String getHumanReadableDescription() {
        return humanReadableDescription;
    }

    
    public void setHumanReadableDescription(String humanReadableDescription) {
        this.humanReadableDescription = humanReadableDescription;
    }

    
    public ResourceContainmentModel getResourceContainmentModel() {
        return resourceContainmentModel;
    }

    
    public void setResourceContainmentModel(
            ResourceContainmentModel resourceContainmentModel) {
        this.resourceContainmentModel = resourceContainmentModel;
    }

    public ResourceMigrationTarget[] getResourceMigrationTargets() {
        return resourceMigrationTargets;
    }

    public void setResourceMigrationTargets(ResourceMigrationTarget[] resourceMigrationTargets) {
        this.resourceMigrationTargets = resourceMigrationTargets;
    }
}
