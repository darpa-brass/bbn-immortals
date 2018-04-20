package com.bbn.marti.immortals.pipelines;

import com.bbn.marti.immortals.converters.SocketToTcpTransport;
import com.bbn.marti.immortals.net.CotServerChannel;
import com.bbn.marti.immortals.net.tcp.TcpSocketServer;
import com.bbn.marti.immortals.pipes.CotByteBufferPipe;
import mil.darpa.immortals.core.synthesis.AbstractFunctionConsumingPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

/**
 * Converts a SocketServer to a CotServerChannel to be used by TAKServer
 * <p>
 * Created by awellman@bbn.com on 1/11/16.
 */
public class TcpSocketServerToCotServerChannel extends AbstractFunctionConsumingPipe<TcpSocketServer, CotServerChannel> {

    public TcpSocketServerToCotServerChannel(ConsumingPipe<CotServerChannel> next) {
        super(true, next);
    }

    @Override
    public CotServerChannel process(TcpSocketServer tss) {

        // Construct an external facing CotChannel to connect to the SubmissionService
        CotServerChannel cotServerChannel = new CotServerChannel();


        // Start listening for clients
        cotServerChannel.setStartListeningListener(
                // When the CotChannel is instructed to start listening, send the instruction to the SocketServer
                tss.startListeningForClients()
        );

        //// Set up new client connections to the SubmissionService
        tss.setOutputClientConnectedListener(
                // When a new client connects, create a client TcpTransport from it
                new SocketToTcpTransport(
                        // When a client TcpTransport is produced, create a channel from it
                        new TcpTransportToCotChannelData(
                                // Send the channel through to the CotServerChannel
                                cotServerChannel.clientConnected()
                        )
                )
        );

        // Send data to the submission service
        tss.setReceiveFromNetworkPipeListener(
                // When the server receives data, send it into the StreamingCotProtocol
                new CotByteBufferPipe(
                        new CotDbInsertionPipe(cotServerChannel.sendCotFromRemoteToLocal())
                )
        );

        return cotServerChannel;
    }
}
