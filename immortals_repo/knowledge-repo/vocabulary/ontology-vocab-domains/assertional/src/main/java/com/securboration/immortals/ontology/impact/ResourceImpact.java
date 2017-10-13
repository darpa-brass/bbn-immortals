package com.securboration.immortals.ontology.impact;

import com.securboration.immortals.ontology.constraint.ResourceImpactType;
import com.securboration.immortals.ontology.core.Resource;

/**
 * Models an impact on an abstract resource
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Models an impact on an abstract resource  @author jstaples ")
public class ResourceImpact extends ImpactStatement {
    
    /**
     * The resource impact statement
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The resource impact statement")
    private ResourceImpactType impactOnResource;
    
    /**
     * The impacted abstract resource
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The impacted abstract resource")
    private Class<? extends Resource> impactedResource;
    
    public ResourceImpactType getImpactOnResource() {
        return impactOnResource;
    }
    
    public void setImpactOnResource(ResourceImpactType impactOnResource) {
        this.impactOnResource = impactOnResource;
    }
    
    public Class<? extends Resource> getImpactedResource() {
        return impactedResource;
    }
    
    public void setImpactedResource(Class<? extends Resource> impactedResource) {
        this.impactedResource = impactedResource;
    }

}
