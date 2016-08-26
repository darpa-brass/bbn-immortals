package com.securboration.immortals.ontology.resources.gps;

/**
 * Describes an environment in which a GPS receiver is expected to operate
 * @author Securboration
 *
 */
public class GpsEnvironment {
    
    /**
     * The number of satellites assumed to be visible
     */
    private int numberOfVisibleSatellites;

    /**
     * Assumptions about nearby structures that may affect the GPS signal
     */
    private Structures structures;
    
    /**
     * Assumptions about terrain that may affect the GPS signal
     */
    private Terrain terrainModel;

    public int getNumberOfVisibleSatellites() {
        return numberOfVisibleSatellites;
    }

    public void setNumberOfVisibleSatellites(int numberOfVisibleSatellites) {
        this.numberOfVisibleSatellites = numberOfVisibleSatellites;
    }

    public Structures getStructures() {
        return structures;
    }

    public void setStructures(Structures structures) {
        this.structures = structures;
    }

    public Terrain getTerrainModel() {
        return terrainModel;
    }

    public void setTerrainModel(Terrain terrainModel) {
        this.terrainModel = terrainModel;
    }
    
}
