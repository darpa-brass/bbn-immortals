package com.securboration.immortals.ontology.resources.environment;

import com.securboration.immortals.ontology.resources.gps.GpsEnvironment;
import com.securboration.immortals.ontology.resources.network.NetworkTopology;

/**
 * A description of the environment in which software is executed
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A description of the environment in which software is executed " +
    " @author jstaples ")
public class OperatingEnvironment {
    
    /**
     * A model of the network topology in the environment
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A model of the network topology in the environment")
    private NetworkTopology network;
    
    /**
     * A model of the gps topology
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "A model of the gps topology")
    private GpsEnvironment gps;

    public NetworkTopology getNetwork() {
        return network;
    }

    public void setNetwork(NetworkTopology network) {
        this.network = network;
    }

    public GpsEnvironment getGps() {
        return gps;
    }

    public void setGps(GpsEnvironment gps) {
        this.gps = gps;
    }
    
}
