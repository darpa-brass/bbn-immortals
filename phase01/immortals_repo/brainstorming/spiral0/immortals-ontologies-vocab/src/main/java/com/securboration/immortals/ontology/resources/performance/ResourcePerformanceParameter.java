package com.securboration.immortals.ontology.resources.performance;

import com.securboration.immortals.ontology.resources.ResourcePerformanceMetric;
import com.securboration.immortals.ontology.resources.ResourcePerformanceMetricInstance;

/**
 * Describes the performance we anticipate from a resource in terms of some
 * metric
 * 
 * @author Securboration
 *
 */
public class ResourcePerformanceParameter {
    
    /**
     * The metric we are using to describe performance as
     */
    private ResourcePerformanceMetric metric;
    
    /**
     * The metric value describing performance
     */
    private ResourcePerformanceMetricInstance value;
    
    /**
     * The type of performance we're describing
     */
    private ResourcePerformanceType type;

    public ResourcePerformanceMetric getMetric() {
        return metric;
    }

    public void setMetric(ResourcePerformanceMetric metric) {
        this.metric = metric;
    }

    public ResourcePerformanceMetricInstance getValue() {
        return value;
    }

    public void setValue(ResourcePerformanceMetricInstance value) {
        this.value = value;
    }

    public ResourcePerformanceType getType() {
        return type;
    }

    public void setType(ResourcePerformanceType type) {
        this.type = type;
    }
    
    
    
    
    
}
