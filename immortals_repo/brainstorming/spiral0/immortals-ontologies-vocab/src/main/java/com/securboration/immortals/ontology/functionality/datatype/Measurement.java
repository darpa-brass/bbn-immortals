package com.securboration.immortals.ontology.functionality.datatype;

/**
 * Representation of a physical location
 * 
 * @author Securboration
 *
 */
public class Measurement extends DataType{
    
    /**
     * When the measurement was made
     */
    private Time timeOfMeasurement;
    
    /**
     * The value measured
     */
    private DataType measurementInstance;

    public Time getTimeOfMeasurement() {
        return timeOfMeasurement;
    }

    public void setTimeOfMeasurement(Time timeOfMeasurement) {
        this.timeOfMeasurement = timeOfMeasurement;
    }

    public DataType getMeasurementInstance() {
        return measurementInstance;
    }

    public void setMeasurementInstance(DataType measurementInstance) {
        this.measurementInstance = measurementInstance;
    }

}
