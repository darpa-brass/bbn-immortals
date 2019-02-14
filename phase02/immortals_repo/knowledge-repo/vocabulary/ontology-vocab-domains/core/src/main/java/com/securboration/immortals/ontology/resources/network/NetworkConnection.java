package com.securboration.immortals.ontology.resources.network;

import com.securboration.immortals.ontology.resources.ExecutionPlatform;
import com.securboration.immortals.ontology.resources.IOResource;

/**
 * A network connection links two devices via some communication medium
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A network connection links two devices via some communication medium " +
    " @author jstaples ")
public class NetworkConnection extends IOResource {

    /**
     * The local device in a network connection
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The local device in a network connection")
    private ExecutionPlatform localDevice;
    
    /**
     * The remote device in a network connection
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The remote device in a network connection")
    private ExecutionPlatform remoteDevice;
    
    /**
     * The network over which communication occurs
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The network over which communication occurs")
    private NetworkStackAbstraction network;
    
    /**
     * True iff this is a one-way connection from local to remote
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "True iff this is a one-way connection from local to remote")
    private boolean isOneWay;

    public ExecutionPlatform getLocalDevice() {
        return localDevice;
    }

    public void setLocalDevice(ExecutionPlatform localDevice) {
        this.localDevice = localDevice;
    }

    public ExecutionPlatform getRemoteDevice() {
        return remoteDevice;
    }

    public void setRemoteDevice(ExecutionPlatform remoteDevice) {
        this.remoteDevice = remoteDevice;
    }

    public NetworkStackAbstraction getNetwork() {
        return network;
    }

    public void setNetwork(NetworkStackAbstraction network) {
        this.network = network;
    }

    
    public boolean isOneWay() {
        return isOneWay;
    }

    
    public void setOneWay(boolean isOneWay) {
        this.isOneWay = isOneWay;
    }

}
