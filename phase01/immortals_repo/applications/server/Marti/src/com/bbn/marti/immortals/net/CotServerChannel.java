package com.bbn.marti.immortals.net;

import com.bbn.cot.CotEventContainer;
import mil.darpa.immortals.core.AbstractOutputProvider;
import mil.darpa.immortals.core.InputProviderInterface;
import com.bbn.marti.immortals.data.CotChannelData;
import com.bbn.marti.immortals.data.CotData;

/**
 *  This appears redundant at first glance, but it makes pipelining far easier since it works as an interface for
 *  TAKServer.
 *
 * Created by awellman@bbn.com on 1/11/16.
 */
public class CotServerChannel extends CotChannel {

    private final ClientConnected clientConnected;
    private final StartListening startListening;

    public ClientConnected clientConnected() {
        return clientConnected;
    }

    public StartListening startListening() {
        return startListening;
    }

    public CotServerChannel() {
        super();
        clientConnected = new ClientConnected();
        startListening = new StartListening();

        serverCotProducerPipe.addSuccessor(new InputProviderInterface<CotEventContainer>() {
            @Override
            public void handleData(CotEventContainer data) {
                System.err.println("Cannot send data out to a server with an accepting socket!");
            }
        });
    }

    public static class ClientConnected extends AbstractOutputProvider<CotChannelData> implements InputProviderInterface<CotChannelData> {

        @Override
        public void handleData(CotChannelData data) {
            distributeResult(data);
        }
    }

    public static class StartListening extends AbstractOutputProvider<Void> implements InputProviderInterface<Void> {

        @Override
        public void handleData(Void aVoid) {
            distributeResult(aVoid);
        }
    }

}
