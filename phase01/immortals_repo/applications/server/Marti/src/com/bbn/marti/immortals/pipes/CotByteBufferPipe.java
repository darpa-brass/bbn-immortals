package com.bbn.marti.immortals.pipes;

import com.bbn.cot.CotEventContainer;
import com.bbn.marti.immortals.cot.StreamingCotProtocol;
import mil.darpa.immortals.core.AbstractInputOutputProvider;
import mil.darpa.immortals.core.InputProviderInterface;
import org.jetbrains.annotations.NotNull;

/**
 * Queues {@link byte[]} received until they are complete CoT events and then outputs them as {@link CotEventContainer}
 * Created by awellman@bbn.com on 1/14/16.
 */

public class CotByteBufferPipe extends AbstractInputOutputProvider<byte[], CotEventContainer> {

    private final StreamingCotProtocol streamingCotProtocol;

    public CotByteBufferPipe() {
        final CotByteBufferPipe self = this;
        streamingCotProtocol = new StreamingCotProtocol();
        streamingCotProtocol.cotEventContainerProduced().addSuccessor(new InputProviderInterface<CotEventContainer>() {
            @Override
            public void handleData(CotEventContainer cotEventContainer) {
                self.distributeResult(cotEventContainer);
            }
        });

    }

    protected void distributeResult(CotEventContainer data) {
        System.out.println("Received CoT message...");
        super.distributeResult(data);
    }


    @Override
    public void handleData(@NotNull byte[] bytes) {
        streamingCotProtocol.onDataReceived(bytes, bytes.length, streamingCotProtocol);

    }
}
