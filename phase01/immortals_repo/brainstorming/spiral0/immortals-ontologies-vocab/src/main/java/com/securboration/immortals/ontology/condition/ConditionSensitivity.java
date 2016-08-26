package com.securboration.immortals.ontology.condition;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.resources.ResourcePerformanceMetric;

/**
 * Specifies that a resource is sensitive to a condition. Further specifies the
 * impact of observing that condition on the resource's performance in terms of
 * some metric.
 * 
 * @author Securboration
 *
 */
public class ConditionSensitivity {

    /**
     * The resource to which the sensitivity will bind
     */
    private Resource resource;

    /**
     * The resource's performance metric to which the sensitivity will bind
     */
    private ResourcePerformanceMetric metric;

    /**
     * An observable real-world condition that impacts the metric for the
     * indicated resource
     */
    private ObservableCondition condition;

    /**
     * Qualitative scale of the impact on the metric
     */
    private ConditionSensitivityImpactType impact;
    
    /**
     * One condition may trigger another
     */
    private ObservableCondition[] triggeredConditions;

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public ResourcePerformanceMetric getMetric() {
        return metric;
    }

    public void setMetric(ResourcePerformanceMetric metric) {
        this.metric = metric;
    }

    public ObservableCondition getCondition() {
        return condition;
    }

    public void setCondition(ObservableCondition condition) {
        this.condition = condition;
    }

    public ConditionSensitivityImpactType getImpact() {
        return impact;
    }

    public void setImpact(ConditionSensitivityImpactType impact) {
        this.impact = impact;
    }

    public ObservableCondition[] getTriggeredConditions() {
        return triggeredConditions;
    }

    public void setTriggeredConditions(ObservableCondition[] triggeredConditions) {
        this.triggeredConditions = triggeredConditions;
    }
}
