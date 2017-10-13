package com.securboration.example2.types;

import java.util.Date;

import com.securboration.example.annotations.SemanticTypeBinding;

@SemanticTypeBinding(semanticType = "file://ontology.immortals.securboration.com/r1.0/Datatypes.owl#SpatialTemporalCoordinate")
public class Coordinates {

    /**
     * 
     * @return the measured latitude
     */
    public @SemanticTypeBinding(semanticType = "file://ontology.immortals.securboration.com/r1.0/Sensors.owl#Latitude") float getLatitude() {
        return 0f;
    }

    /**
     * 
     * @return the measured longitude
     */
    public @SemanticTypeBinding(semanticType = "file://ontology.immortals.securboration.com/r1.0/Sensors.owl#Longitude") float getLongitude() {
        return 0f;
    }

    /**
     * 
     * @return the measured altitude in meters above mean sea level
     */
    public @SemanticTypeBinding(semanticType = "file://ontology.immortals.securboration.com/r1.0/Sensors.owl#Altitude_MSL") float getAltitudeMSL() {
        return 0f;
    }

    /**
     * 
     * @return the GDOP metric for the GPS signal. See <a href=
     *         "https://en.wikipedia.org/wiki/Dilution_of_precision_%28GPS%29">
     *         Dilution of Precision (DOP)</a>
     */
    // The ontology will contain additional triples identifying GDOP as a type
    // of metric for location performance. Somewhere, an application-specific
    // trigger condition must be specified (e.g., if GDOP exceeds 3.5, seek an
    // alternative mechanism for location). Probably doesn't make sense to do
    // this in the code itself but rather in some artifact that exists alongside
    // the code and is specific to a specific set of performance constraints.
    public @SemanticTypeBinding(semanticType = "file://ontology.immortals.securboration.com/r1.0/Sensors.owl#GDOP") double getAccuracyMetric() {
        return 0d;
    }

    /**
     * 
     * @return the timestamp of the location reading
     */
    // The ontology will contain additional triples identifying acquisition time
    // offset as a metric for location performance. We could also have an age
    // limit that triggers a new location provider being swapped in.
    public @SemanticTypeBinding(semanticType = "file://ontology.immortals.securboration.com/r1.0/Sensors.owl#SensorReadTime") Date getAcquisitionTime() {
        return null;
    }
}
