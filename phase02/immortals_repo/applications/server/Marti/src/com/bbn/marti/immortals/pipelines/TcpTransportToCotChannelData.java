package com.bbn.marti.immortals.pipelines;

import com.bbn.marti.immortals.data.CotChannelData;
import com.bbn.marti.immortals.net.CotChannel;
import com.bbn.marti.immortals.net.tcp.TcpTransport;
import com.bbn.marti.immortals.pipes.CotByteBufferPipe;
import com.bbn.marti.immortals.pipes.CotEventContainerBytesExtractionPipe;
import com.bbn.marti.service.MartiMain;
import mil.darpa.immortals.core.synthesis.AbstractFunctionConsumingPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import mil.darpa.immortals.dfus.images.BitmapWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Constructs an {@link CotChannelData} object from a {@link TcpTransport}
 * Created by awellman@bbn.com on 1/11/16.
 */
public class TcpTransportToCotChannelData extends AbstractFunctionConsumingPipe<TcpTransport, CotChannelData> {

    Logger logger = LoggerFactory.getLogger(TcpTransportToCotChannelData.class);

    public TcpTransportToCotChannelData(ConsumingPipe<CotChannelData> next) {
        super(true, next);
    }

    @Override
    public CotChannelData process(TcpTransport tt) {

        // Construct a CotChannel for the client;
        CotChannel channel = new CotChannel();


        // Connect then network connection to the buffer
        tt.setReceiveFromNetworkPipeListener(
                // Connect then network connection to the buffer
                new CotByteBufferPipe(
                        // Connect the buffer to the server
                        channel.sendCotFromRemoteToLocal(),
                        // And connect it to the BitmapWriter
                        new CotEventImageExtractor(
                                new BitmapWriter(Paths.get(MartiMain.getConfig().storageDirectory), new ConsumingPipe<Path>() {
                                    @Override
                                    public void consume(Path path) {
                                        logger.info("Wrote image to '" + path.toString() + "'.");
                                    }

                                    @Override
                                    public void flushPipe() {
                                    }

                                    @Override
                                    public void closePipe() {
                                    }
                                })
                        )
                )
        );

        // Connect the disconnection notification
        tt.setRemoteDisconnectedListener(channel.remoteDisconnected());

        channel.setServerCotProducerListener(
                new CotEventContainerBytesExtractionPipe(
                        tt.sendToNetworkPipe()
                )
        );

        // Construct a CotChannel to provide the complete data for the new client to the server and distribute the result
        return new CotChannelData(channel, tt.getHost(), tt.getName(), tt.getPort());
    }


}
