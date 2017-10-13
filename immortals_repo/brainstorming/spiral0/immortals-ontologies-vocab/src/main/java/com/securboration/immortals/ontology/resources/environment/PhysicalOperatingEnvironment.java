package com.securboration.immortals.ontology.resources.environment;

import com.securboration.immortals.ontology.resources.gps.Structures;
import com.securboration.immortals.ontology.resources.gps.Terrain;

/**
 * Models the physical aspects of a real-world operating environment
 * 
 * @author Securboration
 *
 */
public class PhysicalOperatingEnvironment {
   
    /**
     * The structures in the operating environment
     */
    private Structures structures;
    
    /**
     * The terrain in the operating environment
     */
    private Terrain terrain;

    public Structures getStructures() {
        return structures;
    }

    public void setStructures(Structures structures) {
        this.structures = structures;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }
    
}
