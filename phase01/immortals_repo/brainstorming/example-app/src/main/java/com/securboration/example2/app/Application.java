package com.securboration.example2.app;

import com.securboration.example.annotations.SemanticTypeConverter;
import com.securboration.example2.types.Coordinates;

/**
 * Another example illustrating tradeoffs that are more relevant to the SA
 * domain. Specifically, this example deals with tradeoffs encountered when
 * determining location via:
 * <p>
 * 
 * <ol>
 * <li>Onboard GPS device. Slow, large power requirement, works almost anywhere,
 * usually (but not always) accurate.</li>
 * <li>Wifi/cellular triangulation. Fast, low power requirement, requires
 * network connectivity, ~1km accuracy</li>
 * <li>Manually entered coordinates or locale provided by an operator. Slow,
 * variable accuracy, burdensome, but works anywhere</li>
 * </ol>
 * 
 * @author jstaples
 *
 */
public class Application {

    /**
     * A DFU with dependencies on GPS accuracy and network connectivity
     * 
     * Adaptation mechanism: dependency injection
     */
    private LocationProvider locationProvider;

    /**
     * A DFU with dependencies on network connectivity and the server interface
     * 
     * Adaptation mechanism: dependency injection
     */
    private Client client;

    /**
     * A trivial DFU that serializes some input data
     * 
     * Adaptation mechanism: AOP-style call interception and re-route using a
     * proxy
     * 
     * @param data
     *            a coordinate to serialize
     * @return a serialized representation of the coordinates amenable to
     *         transmission to the server
     */
    @SemanticTypeConverter(
            inputSemanticType="file://ontology.immortals.securboration.com/r1.0/Datatypes.owl#GeoSpatialCoordinate",
            outputSemanticType="file://ontology.immortals.securboration.com/r1.0/ServerModel1516.owl#JsonEncodedGeoSpatialCoordinate")
    private static byte[] serialize(Coordinates data) {
        return null;
    }
    
    /**
     * An intentionally simplistic, hard-coded processing pipeline
     */
    public void functionality() {
        
        // get current GPS coordinates
        Coordinates location = locationProvider.getLocation();

        // serialize the coordinates
        byte[] serializedLocation = serialize(location);

        // transmit serialized location to server
        client.transmit(serializedLocation);
    }

}
