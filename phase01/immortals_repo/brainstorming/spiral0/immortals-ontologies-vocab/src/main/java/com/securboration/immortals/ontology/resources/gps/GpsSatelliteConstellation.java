package com.securboration.immortals.ontology.resources.gps;

import com.securboration.immortals.ontology.core.Resource;

/**
 * a satellite constellation that provides a GPS signal
 * 
 * @author Securboration
 *
 */
public class GpsSatelliteConstellation extends Resource {

    /**
     * The satellites comprising the constellation
     */
    private GpsSatellite[] satellites;

    public GpsSatellite[] getSatellites() {
        return satellites;
    }

    public void setSatellites(GpsSatellite[] satellites) {
        this.satellites = satellites;
    }

}
