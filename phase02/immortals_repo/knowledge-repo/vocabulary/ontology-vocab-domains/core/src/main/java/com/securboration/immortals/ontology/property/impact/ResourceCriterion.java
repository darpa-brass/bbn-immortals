package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.constraint.ResourceCriterionType;
import com.securboration.immortals.ontology.core.Resource;

/**
 * A criterion statement about an abstract resource 
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A criterion statement about an abstract resource   @author jstaples ")
public class ResourceCriterion extends CriterionStatement {

    /**
     * The criterion statement
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The criterion statement")
    private ResourceCriterionType criterion;
    
    /**
     * The abstract resource
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The abstract resource")
    private Class<? extends Resource> resource;
    
    public ResourceCriterionType getCriterion() {
        return criterion;
    }
    
    public void setCriterion(ResourceCriterionType criterion) {
        this.criterion = criterion;
    }

    
    public Class<? extends Resource> getResource() {
        return resource;
    }

    
    public void setResource(Class<? extends Resource> resource) {
        this.resource = resource;
    }
    
    
}
