package com.bbn.marti.immortals.pipelines;

import mil.darpa.immortals.core.AbstractOutputProvider;
import mil.darpa.immortals.core.InputProviderInterface;
import com.bbn.marti.immortals.converters.SocketToTcpTransport;
import com.bbn.marti.immortals.pipes.CotByteBufferPipe;
import com.bbn.marti.immortals.net.CotServerChannel;
import com.bbn.marti.immortals.net.tcp.TcpSocketServer;

/**
 * Converts a SocketServer to a CotServerChannel to be used by TAKServer
 * <p>
 * Created by awellman@bbn.com on 1/11/16.
 */
public class TcpSocketServerToCotServerChannel extends AbstractOutputProvider<CotServerChannel> implements InputProviderInterface<TcpSocketServer> {

    @Override
    public synchronized void handleData(TcpSocketServer data) {

        //// Set up server instances
        SocketToTcpTransport tcpTransportSocketToTcpTransportConverter = new SocketToTcpTransport();
        TcpTransportToCotChannelData tcpChannelFactory = new TcpTransportToCotChannelData();

        // Construct an external facing CotChannel
        CotServerChannel cotServerChannel = new CotServerChannel();

        // Construct a StreamingCotProtocol for the server to buffer received bytes until they are complete cot messages
        CotByteBufferPipe cotByteBufferPipe = new CotByteBufferPipe();

        // When the server receives data, send it into the StreamingCotProtocol
        data.receiveFromNetworkPipe().addSuccessor(cotByteBufferPipe);

        // When the StreamingCotProtocol has data to send, send it out the CotServerChannel
        cotByteBufferPipe.addSuccessor(cotServerChannel.sendCotFromRemoteToLocal());

        // When the CotChannel is instructed to start listening, send the message to the SocketServer
        cotServerChannel.startListening().addSuccessor(data.startListeningForClients());


        //// Set up client connections

        // When a new client connects, create a client TcpTransport from it
        data.outputClientConnected.addSuccessor(tcpTransportSocketToTcpTransportConverter);

        // When a client TcpTransport is produced, create a channel from it and send it though the Server channel
        tcpTransportSocketToTcpTransportConverter.addSuccessor(tcpChannelFactory);
        tcpChannelFactory.addSuccessor(cotServerChannel.clientConnected());

        distributeResult(cotServerChannel);
    }
}
