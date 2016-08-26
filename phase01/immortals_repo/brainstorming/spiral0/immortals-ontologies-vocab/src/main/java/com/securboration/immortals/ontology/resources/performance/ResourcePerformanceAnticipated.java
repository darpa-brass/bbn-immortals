package com.securboration.immortals.ontology.resources.performance;

/**
 * Describes the anticipated performance of a resource in terms of some metric
 * 
 * @author Securboration
 *
 */
public class ResourcePerformanceAnticipated
        extends ResourcePerformance {
    
    /**
     * The time the indicated performance will begin
     */
    private long anticipatedStartTimeEpochMillis;
    
    /**
     * The time the indicated performance will end
     */
    private long anticipatedEndTimeEpochMillis;

    public long getAnticipatedStartTimeEpochMillis() {
        return anticipatedStartTimeEpochMillis;
    }

    public void setAnticipatedStartTimeEpochMillis(
            long anticipatedStartTimeEpochMillis) {
        this.anticipatedStartTimeEpochMillis = anticipatedStartTimeEpochMillis;
    }

    public long getAnticipatedEndTimeEpochMillis() {
        return anticipatedEndTimeEpochMillis;
    }

    public void setAnticipatedEndTimeEpochMillis(
            long anticipatedEndTimeEpochMillis) {
        this.anticipatedEndTimeEpochMillis = anticipatedEndTimeEpochMillis;
    }

}
