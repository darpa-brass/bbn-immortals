package com.securboration.immortals.ontology.analysis;

import com.securboration.immortals.ontology.analysis.profiling.SimpleResourceDependencyAssertion;
import com.securboration.immortals.ontology.measurement.MeasurementProfile;

/**
 * An analysis report conveys all of the information currently contained in the
 * .resourceUsageOutput and .path files
 * 
 * @author jstaples
 *
 */
public class AnalysisReport {
    
    private SimpleResourceDependencyAssertion[] discoveredDependency;
    
    private MeasurementProfile[] measurementProfile;

    
    public SimpleResourceDependencyAssertion[] getDiscoveredDependency() {
        return discoveredDependency;
    }

    
    public void setDiscoveredDependency(
            SimpleResourceDependencyAssertion[] discoveredDependency) {
        this.discoveredDependency = discoveredDependency;
    }

    
    public MeasurementProfile[] getMeasurementProfile() {
        return measurementProfile;
    }

    
    public void setMeasurementProfile(MeasurementProfile[] measurementProfile) {
        this.measurementProfile = measurementProfile;
    }

}
