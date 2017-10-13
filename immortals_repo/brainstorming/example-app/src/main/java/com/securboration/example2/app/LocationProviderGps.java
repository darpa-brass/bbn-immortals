package com.securboration.example2.app;

import com.securboration.example.annotations.FunctionalDfuAspect;
import com.securboration.example.annotations.triple.Triple;
import com.securboration.example2.types.Coordinates;

/**
 * Gathers the last fine-grained GPS coordinates retrieved from the on-device
 * GPS receiver
 * <p>
 * See <a
 * href=http://developer.android.com/guide/topics/location/strategies.html>
 * Android location services</a>
 * 
 * @author jstaples
 *
 */
public class LocationProviderGps {

    @FunctionalDfuAspect(functionalAspectUri = "file://ontology.immortals.securboration.com/r1.0/Sensors.owl#LocationProvider")
    @Triple(
            //subject is implicitly this method
            predicateUri="file://ontology.immortals.securboration.com/r1.0/Ecosystem.owl#HasResourceDependency",
            objectUri="file://ontology.immortals.securboration.com/r1.0/Ecosystem.owl#EmbeddedGpsReceiver"
            )
    public Coordinates getLocation() {
        return null;
    }
}
