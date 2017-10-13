package com.securboration.immortals.ontology.resources.performance;

import com.securboration.immortals.ontology.core.Resource;

/**
 * Describes the performance of a resource in terms of metrics
 * 
 * @author Securboration
 *
 */
public class ResourcePerformance {

    /**
     * The resource for which performance will be described
     */
    private Resource resource;

    /**
     * A parameterization of the metrics for that resource 
     */
    private ResourcePerformanceParameter[] performanceDescription;

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public ResourcePerformanceParameter[] getPerformanceDescription() {
        return performanceDescription;
    }

    public void setPerformanceDescription(
            ResourcePerformanceParameter[] performanceDescription) {
        this.performanceDescription = performanceDescription;
    }

}
