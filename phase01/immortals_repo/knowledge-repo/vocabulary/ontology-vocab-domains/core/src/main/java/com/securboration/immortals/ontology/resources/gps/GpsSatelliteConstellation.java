package com.securboration.immortals.ontology.resources.gps;

import com.securboration.immortals.ontology.core.Resource;

/**
 * a satellite constellation that provides a GPS signal
 * 
 * @author Securboration
 *
 */
public class GpsSatelliteConstellation extends Resource {
    
    private String constellationName;

    /**
     * The satellites comprising the constellation.  Note that these are all 
     * satellites in the constellation, not just the ones visible at a given 
     * place and time.
     */
    private GpsSatellite[] satellites;

    public GpsSatellite[] getSatellites() {
        return satellites;
    }

    public void setSatellites(GpsSatellite[] satellites) {
        this.satellites = satellites;
    }

    
    public String getConstellationName() {
        return constellationName;
    }

    
    public void setConstellationName(String constellationName) {
        this.constellationName = constellationName;
    }

}
