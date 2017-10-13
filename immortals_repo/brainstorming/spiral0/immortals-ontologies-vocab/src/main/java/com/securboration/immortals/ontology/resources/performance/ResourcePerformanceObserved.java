package com.securboration.immortals.ontology.resources.performance;

/**
 * Describes the observed performance of a resource in terms of some metric
 * 
 * @author Securboration
 *
 */
public class ResourcePerformanceObserved extends ResourcePerformance {

    /**
     * The time the performance is observed
     */
    private long observationTimeEpochMillis;

    public long getObservationTimeEpochMillis() {
        return observationTimeEpochMillis;
    }

    public void setObservationTimeEpochMillis(long observationTimeEpochMillis) {
        this.observationTimeEpochMillis = observationTimeEpochMillis;
    }
    
}
