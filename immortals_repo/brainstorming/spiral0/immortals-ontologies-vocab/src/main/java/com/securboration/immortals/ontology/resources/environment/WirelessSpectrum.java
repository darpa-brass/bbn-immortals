package com.securboration.immortals.ontology.resources.environment;

/**
 * A spectrum block
 * 
 * @author Securboration
 *
 */
public class WirelessSpectrum {
    
    /**
     * The lower bound of the spectrum block
     */
    private double minFrequency;
    
    /**
     * The upper bound of the spectrum block
     */
    private double maxFrequency;

    public double getMinFrequency() {
        return minFrequency;
    }

    public void setMinFrequency(double minFrequency) {
        this.minFrequency = minFrequency;
    }

    public double getMaxFrequency() {
        return maxFrequency;
    }

    public void setMaxFrequency(double maxFrequency) {
        this.maxFrequency = maxFrequency;
    }
    
}
