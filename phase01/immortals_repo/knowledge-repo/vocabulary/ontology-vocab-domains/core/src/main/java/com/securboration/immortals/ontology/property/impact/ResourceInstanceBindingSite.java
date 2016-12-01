package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.core.Resource;

/**
 * A resource instance to which an assertive statement can bind 
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A resource instance to which an assertive statement can bind   @author" +
    " jstaples ")
public class ResourceInstanceBindingSite extends AssertionBindingSite {
    
    /**
     * The resource instance to which the assertive statement binds
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The resource instance to which the assertive statement binds")
    private Resource resourceInstance;

    
    public Resource getResourceInstance() {
        return resourceInstance;
    }

    
    public void setResourceInstance(Resource resourceInstance) {
        this.resourceInstance = resourceInstance;
    }
    
    
}
