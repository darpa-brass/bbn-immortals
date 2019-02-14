package com.bbn.marti.immortals.net;

import com.bbn.cot.CotEventContainer;
import com.bbn.marti.immortals.data.CotData;
import mil.darpa.immortals.core.synthesis.AbstractFunctionConsumingPipe;
import mil.darpa.immortals.core.synthesis.AbstractMultisuccessorConsumingPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

/**
 * This appears redundant at first glance, but it makes pipelining far easier when you want to modify the input and
 * output channels separately.
 * <p/>
 * Created by awellman@bbn.com on 1/11/16.
 */
public class CotChannel {

    protected ConsumingPipe<CotEventContainer> serverCotProducerPipe;
    protected final ServerCotConsumerPipe serverCotConsumerPipe;
    protected final RemoteDisconnected remoteDisconnected;


    public void setServerCotProducerListener(ConsumingPipe<CotEventContainer> listener) {
        serverCotProducerPipe = listener;
    }

    public ServerCotConsumerPipe sendCotFromRemoteToLocal() {
        return serverCotConsumerPipe;
    }

    public RemoteDisconnected remoteDisconnected() {
        return remoteDisconnected;
    }

    public CotChannel() {
        serverCotConsumerPipe = new ServerCotConsumerPipe(this, null);
        remoteDisconnected = new RemoteDisconnected(this, null);
    }

    /**
     * Produces {@link CotEventContainer} produced by the server
     */
    public void handleCotData(CotData data) {
        serverCotProducerPipe.consume(data.cotEventContainer);
    }



    /**
     * Consumes {@link CotEventContainer} events on behalf of the server
     */
    public static class ServerCotConsumerPipe extends AbstractMultisuccessorConsumingPipe<CotEventContainer, CotData> {

        private CotChannel cotChannel;

        public ServerCotConsumerPipe(CotChannel cotChannel, ConsumingPipe<CotData> next) {
            super(true, next);
            this.cotChannel = cotChannel;
        }

        @Override
        public CotData process(CotEventContainer input) {
            return new CotData(input, cotChannel);
        }
    }


    public static class RemoteDisconnected extends AbstractFunctionConsumingPipe<Void, CotChannel> {

        private CotChannel cotChannel;

        public RemoteDisconnected(CotChannel cotChannel, ConsumingPipe<CotChannel> next) {
            super(true, next);
            this.cotChannel = cotChannel;
        }

        @Override
        public CotChannel process(Void Void) {
            return cotChannel;
        }
    }

}
