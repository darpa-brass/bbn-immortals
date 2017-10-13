package com.securboration.example2.app;

import com.securboration.example.annotations.FunctionalDfuAspect;
import com.securboration.example.annotations.triple.Triple;
import com.securboration.example2.types.Coordinates;

/**
 * Gathers coarse-grained GPS coordinates using wifi and cellular connections
 * <p>
 * See <a
 * href=http://developer.android.com/guide/topics/location/strategies.html>
 * Android location services</a>
 * 
 * @author Securboration
 *
 */
public class LocationProviderNetwork {

    @FunctionalDfuAspect(functionalAspectUri = "file://ontology.immortals.securboration.com/r1.0/Sensors.owl#LocationProvider")
    @Triple(
            //subject is implicitly this method
            predicateUri="file://ontology.immortals.securboration.com/r1.0/Ecosystem.owl#HasResourceDependency",
            objectUri="file://ontology.immortals.securboration.com/r1.0/Ecosystem.owl#NetworkTriangulationService"
            )
    @Triple(
            //subject is implicitly this method
            predicateUri="file://ontology.immortals.securboration.com/r1.0/Ecosystem.owl#HasResourceDependency",
            objectUri="file://ontology.immortals.securboration.com/r1.0/Ecosystem.owl#CellularTriangulationService"
            )
    public Coordinates getLocation() {
        return null;
    }
}
