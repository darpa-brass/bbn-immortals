package com.bbn.marti.net;

import com.bbn.marti.immortals.net.tcp.Transport;

/**
 * Callback interface that, once implemented, describes how to handle data from a transport.
 *
 * @param <T>
 * @author kusbeck
 */
public interface ProtocolListener<T> {

    /**
     * Called when data is received by transport
     *
     * @param data      The data received
     * @param transport The transport over which data was received
     * @param protocol  The protocol over which data was received
     */
    void onDataReceived(T data, final Transport transport, final com.bbn.marti.immortals.net.Protocol<T> protocol);
}
