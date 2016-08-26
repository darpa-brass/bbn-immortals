package com.securboration.example2.app;

import com.securboration.example.annotations.Dfu;
import com.securboration.example.annotations.SemanticTypeBinding;
import com.securboration.example2.types.Coordinates;

/**
 * An interface to be implemented by a location provider
 * 
 * @author jstaples
 *
 */
@Dfu(functionalityUri = "file://ontology.immortals.securboration.com/r1.0/Sensors.owl#LocationProvider")
public interface LocationProvider {

    /**
     * 
     * @return the last known location of this device
     */
    public @SemanticTypeBinding(semanticType = "file://ontology.immortals.securboration.com/r1.0/Sensors.owl#SpatialTemporalCoordinate") Coordinates getLocation();
}
