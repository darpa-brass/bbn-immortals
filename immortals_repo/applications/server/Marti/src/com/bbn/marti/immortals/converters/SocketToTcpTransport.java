package com.bbn.marti.immortals.converters;

import com.bbn.marti.immortals.data.SocketData;
import com.bbn.marti.immortals.data.TcpInitializationData;
import com.bbn.marti.immortals.net.tcp.TcpTransport;
import mil.darpa.immortals.core.synthesis.AbstractFunctionConsumingPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import java.io.IOException;

/**
 * Created by awellman@bbn.com on 1/12/16.
 */
public class SocketToTcpTransport extends AbstractFunctionConsumingPipe<SocketData, TcpTransport> {

    public SocketToTcpTransport(ConsumingPipe<TcpTransport> next) {
        super(true, next);
    }

    @Override
    public TcpTransport process(SocketData input) {
        final TcpTransport transport;
        // Construct a transport
        TcpInitializationData initData = new TcpInitializationData(input.sourceServerIdentifier, input.protocol, input.socket.getInetAddress(), input.socket.getPort());
        transport = new TcpTransport(initData);
        transport.clientSocket = input.socket;
        return transport;
    }
}
