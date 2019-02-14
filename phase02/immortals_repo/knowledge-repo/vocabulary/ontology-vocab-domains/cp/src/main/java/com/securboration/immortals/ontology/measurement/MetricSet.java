package com.securboration.immortals.ontology.measurement;

/**
 * Aggregation unit for metric profiles
 * 
 * @author jstaples
 *
 */
public class MetricSet {
    
    private MetricProfile[] profiles;

    
    public MetricProfile[] getProfiles() {
        return profiles;
    }

    
    public void setProfiles(MetricProfile[] profiles) {
        this.profiles = profiles;
    }
    
}
