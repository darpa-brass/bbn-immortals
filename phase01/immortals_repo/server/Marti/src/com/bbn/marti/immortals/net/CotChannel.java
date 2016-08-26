package com.bbn.marti.immortals.net;

import com.bbn.cot.CotEventContainer;
import com.bbn.marti.immortals.data.CotData;
import mil.darpa.immortals.core.AbstractOutputProvider;
import mil.darpa.immortals.core.InputProviderInterface;

/**
 * This appears redundant at first glance, but it makes pipelining far easier when you want to modify the input and
 * output channels separately.
 * <p/>
 * Created by awellman@bbn.com on 1/11/16.
 */
public class CotChannel {

    protected final ServerCotProducerPipe serverCotProducerPipe;
    protected final ServerCotConsumerPipe serverCotConsumerPipe;
    protected final RemoteDisconnected remoteDisconnected;

    public ServerCotProducerPipe sendCotFromLocalToRemote() {
        return serverCotProducerPipe;
    }

    public ServerCotConsumerPipe sendCotFromRemoteToLocal() {
        return serverCotConsumerPipe;
    }

    public RemoteDisconnected remoteDisconnected() {
        return remoteDisconnected;
    }

    public CotChannel() {
        serverCotProducerPipe = new ServerCotProducerPipe(this);
        serverCotConsumerPipe = new ServerCotConsumerPipe(this);
        remoteDisconnected = new RemoteDisconnected(this);
    }

    /**
     * Produces {@link CotEventContainer} produced by the server
     */
    public static class ServerCotProducerPipe extends AbstractOutputProvider<CotEventContainer> implements InputProviderInterface<CotData> {

        private CotChannel cotChannel;

        public ServerCotProducerPipe(CotChannel cotChannel) {
            this.cotChannel = cotChannel;
        }

        @Override
        public void handleData(CotData data) {
            distributeResult(data.cotEventContainer);

        }
    }

    /**
     * Consumes {@link CotEventContainer} events on behalf of the server
     */
    public static class ServerCotConsumerPipe extends AbstractOutputProvider<CotData> implements InputProviderInterface<CotEventContainer> {

        private CotChannel cotChannel;

        public ServerCotConsumerPipe(CotChannel cotChannel) {
            this.cotChannel = cotChannel;
        }

        @Override
        public void handleData(CotEventContainer data) {
            CotData cotData = new CotData(data, cotChannel);
            distributeResult(cotData);
        }
    }

    public static class RemoteDisconnected extends AbstractOutputProvider<CotChannel> implements InputProviderInterface<Void> {

        private CotChannel cotChannel;

        protected RemoteDisconnected(CotChannel cotChannel) {
            this.cotChannel = cotChannel;
        }

        @Override
        public void handleData(Void data) {
            distributeResult(cotChannel);
        }
    }

}
