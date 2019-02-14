package com.bbn.marti.immortals.converters;

import com.bbn.marti.immortals.data.TcpInitializationData;
import com.bbn.marti.immortals.net.tcp.TcpSocketServer;
import mil.darpa.immortals.core.synthesis.AbstractFunctionConsumingPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import java.io.IOException;

/**
 * Created by awellman@bbn.com on 1/12/16.
 */
public class TcpInitializationDataToTcpSocketServer extends AbstractFunctionConsumingPipe<TcpInitializationData, TcpSocketServer> {

    public TcpInitializationDataToTcpSocketServer(ConsumingPipe<TcpSocketServer> next) {
        super(true, next);
    }

    @Override
    public TcpSocketServer process(TcpInitializationData input) {
        try {
            if (input.protocol.toLowerCase().equals("stcp")) {
                return new TcpSocketServer(input);
            } else {
                throw new RuntimeException("Invalid protocol " + input.protocol + " passed to TCPSocketServer!");
            }
        } catch (IOException e) {
            // TODO: Immortals: Exception handling
            throw new RuntimeException(e);
        }
    }
}
