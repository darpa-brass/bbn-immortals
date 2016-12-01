package com.securboration.immortals.ontology.resources.network;

import com.securboration.immortals.ontology.resources.NetworkResource;

/**
 * Standard OSI model of a network stack
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Standard OSI model of a network stack  @author jstaples ")
public class NetworkStackAbstraction extends NetworkResource {

    /**
     * Describes how applications will interpret the data
     * 
     * E.g., HTTP, HTTPS
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Describes how applications will interpret the data  E.g., HTTP," +
        " HTTPS")
    private ApplicationLayerAbstraction applicationLayer;

    /**
     * Describes how messages will be reliably delivered in an environment where
     * individual messages may be lost or corrupted.
     * 
     * E.g., TCP, UPD
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Describes how messages will be reliably delivered in an" +
        " environment where individual messages may be lost or corrupted. " +
        " E.g., TCP, UPD")
    private TransportLayerAbstraction transportLayer;

    /**
     * Describes how messages will be routed between two arbitrary points in a
     * network of connected nodes
     * 
     * E.g., IPV6
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Describes how messages will be routed between two arbitrary points" +
        " in a network of connected nodes  E.g., IPV6")
    private NetworkLayerAbstraction internetLayer;

    /**
     * Describes how two nodes connected by some medium will communicate
     * 
     * E.g., IEEE 802.2
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Describes how two nodes connected by some medium will communicate " +
        " E.g., IEEE 802.2")
    private LinkLayerAbstraction linkLayer;

    /**
     * Describes how data is physically transmitted over some medium
     * 
     * E.g., USB, Ethernet
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Describes how data is physically transmitted over some medium " +
        " E.g., USB, Ethernet")
    private PhysicalLayerAbstraction physicalLayer;

    public ApplicationLayerAbstraction getApplicationLayer() {
        return applicationLayer;
    }

    public void setApplicationLayer(ApplicationLayerAbstraction applicationLayer) {
        this.applicationLayer = applicationLayer;
    }

    public TransportLayerAbstraction getTransportLayer() {
        return transportLayer;
    }

    public void setTransportLayer(TransportLayerAbstraction transportLayer) {
        this.transportLayer = transportLayer;
    }

    public NetworkLayerAbstraction getInternetLayer() {
        return internetLayer;
    }

    public void setInternetLayer(NetworkLayerAbstraction internetLayer) {
        this.internetLayer = internetLayer;
    }

    public LinkLayerAbstraction getLinkLayer() {
        return linkLayer;
    }

    public void setLinkLayer(LinkLayerAbstraction linkLayer) {
        this.linkLayer = linkLayer;
    }

    public PhysicalLayerAbstraction getPhysicalLayer() {
        return physicalLayer;
    }

    public void setPhysicalLayer(PhysicalLayerAbstraction physicalLayer) {
        this.physicalLayer = physicalLayer;
    }
}
