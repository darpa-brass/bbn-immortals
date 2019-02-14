package com.securboration.immortals.ontology.measurement;

public class MeasurementProfile {
    
    /**
     * The code unit this measurement binds to
     */
    private CodeUnitPointer codeUnit;
    
    /**
     * Measurements made about the dfu
     */
    private MeasurementInstance[] measurement;

    
    public MeasurementInstance[] getMeasurement() {
        return measurement;
    }

    
    public void setMeasurement(MeasurementInstance[] measurement) {
        this.measurement = measurement;
    }


    
    public CodeUnitPointer getCodeUnit() {
        return codeUnit;
    }


    
    public void setCodeUnit(CodeUnitPointer codeUnit) {
        this.codeUnit = codeUnit;
    }
    
}
