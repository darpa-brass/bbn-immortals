package com.bbn.marti.immortals.pipelines;

import com.bbn.cot.CotEventContainer;
import com.bbn.marti.immortals.converters.SocketToTcpTransport;
import com.bbn.marti.immortals.net.CotServerChannel;
import com.bbn.marti.immortals.net.tcp.TcpSocketServer;
import com.bbn.marti.immortals.pipes.CotByteBufferPipe;
import com.bbn.marti.immortals.pipes.CotEventContainerToCotDataEvent;
import com.bbn.marti.service.MartiMain;
import mil.darpa.immortals.core.synthesis.AbstractFunctionConsumingPipe;
import mil.darpa.immortals.core.synthesis.ConsumingDistributorPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import mil.darpa.immortals.dfus.TakServerDataManager.CotDbConsumer;

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


        ConsumingDistributorPipe<CotEventContainer> dp;

        // Send data to the submission service
        tss.setReceiveFromNetworkPipeListener(
                // When the server receives data, send it into the StreamingCotProtocol
                new CotByteBufferPipe(
                        dp = new ConsumingDistributorPipe<>(false,
                                // When the StreamingCotProtocol has data to send, send it out the CotServerChannel
                                cotServerChannel.sendCotFromRemoteToLocal()
                        )
                )
        );

        // If the database is enabled, add it as an endpoint
        if (MartiMain.getConfig().postGreSqlConfig.enabled) {
            dp.addNext(new CotEventContainerToCotDataEvent(
                    new CotDbConsumer()
            ));
        }

        return cotServerChannel;
    }
}
