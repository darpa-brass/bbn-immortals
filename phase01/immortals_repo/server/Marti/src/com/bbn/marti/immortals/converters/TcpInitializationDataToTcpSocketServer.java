package com.bbn.marti.immortals.converters;

import mil.darpa.immortals.core.AbstractOutputProvider;
import mil.darpa.immortals.core.InputProviderInterface;
import com.bbn.marti.immortals.data.TcpInitializationData;
import com.bbn.marti.immortals.net.tcp.TcpSocketServer;

import java.io.IOException;

/**
 * Created by awellman@bbn.com on 1/12/16.
 */
public class TcpInitializationDataToTcpSocketServer extends AbstractOutputProvider<TcpSocketServer> implements InputProviderInterface<TcpInitializationData> {

    @Override
    public void handleData(TcpInitializationData data) {
        try {
            if (data.protocol.toLowerCase().equals("stcp")) {
                TcpSocketServer server = new TcpSocketServer(data);
                distributeResult(server);
            } else {
                System.err.println("Invalid protocol " + data.protocol + " passed to TCPSocketServer!");
            }
        } catch (IOException e) {
            // TODO: Immortals: Exception handling
            throw new RuntimeException(e);
        }
    }
}
