package com.securboration.immortals.ontology.metrics;

public class VersioningMetric {
    
    /**
     * The type of measurement being made
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
            "The type of measurement being made")
    private MeasurementType measurementType;

    /**
     * The value of the measurement (e.g., "10", "false", "PARKED")
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
            "The value of the measurement (e.g., \"10\", \"false\", \"PARKED\")")
    private String value;
    
    
    private String libraryName;

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public MeasurementType getMeasurementType() {
        return measurementType;
    }

    public void setMeasurementType(MeasurementType measurementType) {
        this.measurementType = measurementType;
    }
}
