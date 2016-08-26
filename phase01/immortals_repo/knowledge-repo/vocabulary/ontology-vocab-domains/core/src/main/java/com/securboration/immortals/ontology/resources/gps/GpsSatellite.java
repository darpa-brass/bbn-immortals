package com.securboration.immortals.ontology.resources.gps;

import com.securboration.immortals.ontology.core.Resource;

/**
 * a satellite that provides a GPS signal
 * 
 * @author Securboration
 *
 */
public class GpsSatellite extends Resource {

    /**
     * A unique identifier for a statellite
     */
    private String satelliteId;

    /**
     * true iff this satellite provides fine-grained location services to
     * compatible military hardware
     */
    private boolean hasFineGrainedEncryptedLocation;

    public String getSatelliteId() {
        return satelliteId;
    }

    public void setSatelliteId(String satelliteId) {
        this.satelliteId = satelliteId;
    }

    public boolean isHasFineGrainedEncryptedLocation() {
        return hasFineGrainedEncryptedLocation;
    }

    public void setHasFineGrainedEncryptedLocation(
            boolean hasFineGrainedEncryptedLocation) {
        this.hasFineGrainedEncryptedLocation = hasFineGrainedEncryptedLocation;
    }

}
