package com.securboration.immortals.ontology.functionality.datatype;

/**
 * Representation of a window of time
 * 
 * @author Securboration
 *
 */
public class TimeRange {

    /**
     * The start of the time range
     */
    private Time startTime;
    
    /**
     * The end of the time range
     */
    private Time endTime;

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }
    
}
