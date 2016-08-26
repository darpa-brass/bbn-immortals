package com.securboration.immortals.ontology.resources.network;

import com.securboration.immortals.ontology.resources.PlatformResource;

/**
 * Models the connections available in the network
 * 
 * @author Securboration
 *
 */
public class NetworkTopology extends PlatformResource {

    /**
     * The network topology is simplified by assuming it can be viewed as a set
     * of available connections, each with their own performance characteristics
     */
    private NetworkConnection[] connections;

    public NetworkConnection[] getConnections() {
        return connections;
    }

    public void setConnections(NetworkConnection[] connections) {
        this.connections = connections;
    }

}
