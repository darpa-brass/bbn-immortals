package com.bbn.marti.immortals.converters;

import mil.darpa.immortals.core.AbstractOutputProvider;
import mil.darpa.immortals.core.InputProviderInterface;
import com.bbn.marti.immortals.data.TcpInitializationData;
import com.bbn.marti.immortals.data.SocketData;
import com.bbn.marti.immortals.net.tcp.TcpTransport;

import java.io.IOException;

/**
 * Created by awellman@bbn.com on 1/12/16.
 */
public class SocketToTcpTransport extends AbstractOutputProvider<TcpTransport> implements InputProviderInterface<SocketData> {

    @Override
    public void handleData(SocketData data) {
        final TcpTransport transport;
        try {
            // Construct a transport
            TcpInitializationData initData = new TcpInitializationData(data.sourceServerIdentifier, data.protocol, data.socket.getInetAddress(), data.socket.getPort());
            transport = new TcpTransport(initData);
            transport.clientSocket = data.socket;
            transport.startListening(data.socket);
            distributeResult(transport);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
