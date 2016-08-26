package com.securboration.immortals.ontology.functionality.dataproperties;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.dataproperties.ImpactType;

/**
 * Describes an impact of varying fidelity on a consumed resource.
 * 
 * E.g., {INCREASE FIDELITY_IMAGE_SIZE : INCREASE MEMORY} means that increasing
 * image size will result in increasing memory utilization
 * 
 * @author Securboration
 *
 */
public class FidelityResourceRelationship {
    
    private ImpactType drivingCondition;
    private Class<? extends Fidelity> drivenFidelity;
    
    private ImpactType impactOnResource;
    private Class<? extends Resource> impactedResource;
    
    public ImpactType getDrivingCondition() {
        return drivingCondition;
    }
    
    public void setDrivingCondition(ImpactType drivingCondition) {
        this.drivingCondition = drivingCondition;
    }
    
    public Class<? extends Fidelity> getDrivenFidelity() {
        return drivenFidelity;
    }
    
    public void setDrivenFidelity(Class<? extends Fidelity> drivenFidelity) {
        this.drivenFidelity = drivenFidelity;
    }
    
    public ImpactType getImpactOnResource() {
        return impactOnResource;
    }
    
    public void setImpactOnResource(ImpactType impactOnResource) {
        this.impactOnResource = impactOnResource;
    }
    
    public Class<? extends Resource> getImpactedResource() {
        return impactedResource;
    }
    
    public void setImpactedResource(Class<? extends Resource> impactedResource) {
        this.impactedResource = impactedResource;
    }

}
