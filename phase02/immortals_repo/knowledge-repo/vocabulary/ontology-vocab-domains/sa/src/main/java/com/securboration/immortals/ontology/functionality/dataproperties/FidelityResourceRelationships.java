package com.securboration.immortals.ontology.functionality.dataproperties;

/**
 * Describes an impact of varying fidelity on a consumed resource.
 * 
 * E.g., {INCREASE FIDELITY_IMAGE_SIZE : INCREASE MEMORY} means that increasing
 * image size will result in increasing memory utilization
 * 
 * @author Securboration
 *
 */
public class FidelityResourceRelationships {
    
    private FidelityResourceRelationship[] relationships;

    
    public FidelityResourceRelationship[] getRelationships() {
        return relationships;
    }

    
    public void setRelationships(FidelityResourceRelationship[] relationships) {
        this.relationships = relationships;
    }
    
}
