package com.securboration.immortals.ontology.functionality.datatype;

/**
 * Representation of a physical location
 * 
 * @author Securboration
 *
 */
public class PhysicalLocationMeasurement extends Measurement{
    
    /**
     * The location observed
     */
    private PhysicalLocation locationObserved;
    
    /**
     * The time observed
     */
    private Time timeObserved;

    public PhysicalLocation getLocationObserved() {
        return locationObserved;
    }

    public void setLocationObserved(PhysicalLocation locationObserved) {
        this.locationObserved = locationObserved;
    }

    public Time getTimeObserved() {
        return timeObserved;
    }

    public void setTimeObserved(Time timeObserved) {
        this.timeObserved = timeObserved;
    }

}
