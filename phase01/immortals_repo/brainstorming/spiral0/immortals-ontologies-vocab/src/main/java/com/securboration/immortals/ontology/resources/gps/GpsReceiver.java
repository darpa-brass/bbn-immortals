package com.securboration.immortals.ontology.resources.gps;

import com.securboration.immortals.ontology.core.Resource;

/**
 * Models a GPS receiver
 * 
 * @author Securboration
 *
 */
public class GpsReceiver extends Resource{
    
    /**
     * # of satellites the GPS receiver can lock onto
     * e.g., cheapo GPS receivers use 4 whereas military grade can use 6+
     */
    private int numChannels;
    
    /**
     * The constellation to which this receiver connects
     */
    private GpsSatelliteConstellation constellation;

    public int getNumChannels() {
        return numChannels;
    }

    public void setNumChannels(int numChannels) {
        this.numChannels = numChannels;
    }

    public GpsSatelliteConstellation getConstellation() {
        return constellation;
    }

    public void setConstellation(GpsSatelliteConstellation constellation) {
        this.constellation = constellation;
    }
    
}
