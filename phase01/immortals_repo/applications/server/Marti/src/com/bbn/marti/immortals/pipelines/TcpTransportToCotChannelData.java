package com.bbn.marti.immortals.pipelines;

import com.bbn.cot.CotEventContainer;
import com.bbn.marti.immortals.data.CotChannelData;
import com.bbn.marti.immortals.net.CotChannel;
import com.bbn.marti.immortals.net.tcp.TcpTransport;
import com.bbn.marti.immortals.net.tcp.Transport;
import com.bbn.marti.immortals.pipes.CotByteBufferPipe;
import com.bbn.marti.immortals.pipes.CotEventContainerBytesExtractionPipe;
import mil.darpa.immortals.core.AbstractInputOutputProvider;
import mil.darpa.immortals.core.AbstractOutputProvider;
import mil.darpa.immortals.core.InputProviderInterface;

/**
 * Constructs an {@link CotChannelData} object from a {@link TcpTransport}
 * Created by awellman@bbn.com on 1/11/16.
 */
public class TcpTransportToCotChannelData extends AbstractOutputProvider<CotChannelData> implements InputProviderInterface<TcpTransport> {

    @Override
    public synchronized void handleData(TcpTransport data) {

        // Construct a CotChannel for the client;
        CotChannel channel = new CotChannel();

        // Connect the disconnection notification
        data.remoteDisconnected().addSuccessor(channel.remoteDisconnected());


        //// *** DFU Declaration BEGIN *** ////

        // Declare the client connections
        Transport.ReceiveFromNetworkPipe clientByteProducerPipe = data.receiveFromNetworkPipe();
        Transport.SendToNetworkPipe clientByteConsumerPipe = data.sendToNetworkPipe();

        // Declare the server connections
        CotChannel.ServerCotConsumerPipe serverCotConsumerPipe = channel.sendCotFromRemoteToLocal();
        CotChannel.ServerCotProducerPipe serverCotProducerPipe = channel.sendCotFromLocalToRemote();

        //// *** DFU Declaration END *** ////


        //// *** Control Point SI BEGIN *** (pipeline construction) ////

        // SI-work: D637B79D-7D49-4148-B9B1-942C84DCB647
        AbstractInputOutputProvider<byte[], CotEventContainer> siPipe = null;
        siPipe = new CotByteBufferPipe();
        // SI-work-end


        // Connect then network connection to the buffer
        clientByteProducerPipe.addSuccessor(siPipe);

        // Connect the buffer to the server
        siPipe.addSuccessor(serverCotConsumerPipe);

        // Take note if this is enabled it needs to be inserted between the siPipe and serverCotConsumerPipe
        InFromNetworkImageProcessor inFromNetworkImageProcessor = new InFromNetworkImageProcessor();
        siPipe.addSuccessor(inFromNetworkImageProcessor);
        inFromNetworkImageProcessor.addSuccessor(serverCotConsumerPipe);


        //// *** Control Point SI END *** (pipeline construction) ////


        // *** Control Point SO BEGIN *** (pipeline construction) ////

        // SO-work: DA69DD7D-051A-4549-A4F7-63B19B4BFDF1
        AbstractInputOutputProvider<CotEventContainer, byte[]> soPipe = null;
        soPipe = new CotEventContainerBytesExtractionPipe();

        // SO-work-end

        // Connect the server cot producer to the converter
        serverCotProducerPipe.addSuccessor(soPipe);

        // Connect the converter to the client consumer pipe
        soPipe.addSuccessor(clientByteConsumerPipe);
        // SO-init-end


        // Take note if this is enabled it needs to be inserted between the serverCotProducerPipe and the soPipe
//        OutToNetworkImageProcessor outToNetworkImageProcessor = new OutToNetworkImageProcessor();
//        serverCotProducerPipe.addSuccessor(outToNetworkImageProcessor);
//        outToNetworkImageProcessor.addSuccessor(soPipe);

        // *** Control Point SO END *** (pipeline construction) ////


        // Construct a CotChannel to provide the complete data for the new client to the server and distribute the result
        CotChannelData cotChannelData = new CotChannelData(channel, data.getHost(), data.getName(), data.getPort());

        distributeResult(cotChannelData);
    }
}
