package com.securboration.example2.app;

import com.securboration.example.annotations.FunctionalDfuAspect;
import com.securboration.example.annotations.triple.Triple;
import com.securboration.example2.types.Coordinates;

/**
 * Gathers the last manually-entered location (defined by an operator in the
 * app's UI)
 * 
 * @author jstaples
 *
 */
public class LocationProviderManual {

    @FunctionalDfuAspect(functionalAspectUri = "file://ontology.immortals.securboration.com/r1.0/Sensors.owl#LocationProvider")
    @Triple(
            //subject is implicitly this method
            predicateUri="file://ontology.immortals.securboration.com/r1.0/Ecosystem.owl#HasResourceDependency",
            objectUri="file://ontology.immortals.securboration.com/r1.0/Ecosystem.owl#OperatorInput"
            )
    public Coordinates getLocation() {
        return null;
    }
}
