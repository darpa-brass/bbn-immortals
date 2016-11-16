package com.bbn.marti.immortals.net.tcp;

import mil.darpa.immortals.core.InputProviderInterface;
import mil.darpa.immortals.core.OutputProviderInterface;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Interface for a network {@link Transport}
 *
 * @author kusbeck
 */
interface TransportInterface {

    /**
     * @return the local (network) port that is being used by this transport.
     */
    int getLocalPort();

    /**
     * Select a host and a port over which future connections will be made
     *
     * @param host
     * @param port
     * @return the transport with endpoint set
     */
    void setEndpoint(InetAddress host, int port) throws IOException;

    /**
     * Implements listening logic on this transport.
     * This call MUST be asynchronous, meaning that it MUST return immediately
     * and the listener should call {@link Transport#dataReceived(byte[], int)}
     * when it receives data.
     */
    void startListening() throws IOException;

    /**
     * Implements listening logic on this transport, initializing in the
     * spawned thread with the supplied socket
     */
    void startListening(Socket s)
            throws IOException;

    /**
     * @return Host to which this transport is connected / will connect
     */
    InetAddress getHost();

    /**
     * @return Port to which this transport is connected / will connect
     */
    int getPort();

    /**
     * @return String representation suitable for debugging
     */
    String toString();

    OutputProviderInterface<byte[]> receiveFromNetworkPipe();

    OutputProviderInterface<Void> remoteDisconnected();

    InputProviderInterface<byte[]> sendToNetworkPipe();

}
