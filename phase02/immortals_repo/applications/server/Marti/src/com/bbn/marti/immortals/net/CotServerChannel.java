package com.bbn.marti.immortals.net;

import com.bbn.cot.CotEventContainer;
import com.bbn.marti.immortals.data.CotChannelData;
import mil.darpa.immortals.core.synthesis.AbstractFunctionConsumingPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

/**
 * This appears redundant at first glance, but it makes pipelining far easier since it works as an interface for
 * TAKServer.
 * <p>
 * Created by awellman@bbn.com on 1/11/16.
 */
public class CotServerChannel extends CotChannel {

    private final ClientConnected clientConnected;
    private final StartListening startListening;

    public ClientConnected clientConnected() {
        return clientConnected;
    }
    
    public StartListening getStartListening() {
        return startListening;
    }

    public void setStartListeningListener(ConsumingPipe<Void> next) {
        startListening.setNext(next);
    }

    public CotServerChannel() {
        super();
        clientConnected = new ClientConnected(null);
        startListening = new StartListening(null);

    }

    public static class ClientConnected extends AbstractFunctionConsumingPipe<CotChannelData, CotChannelData> {

        public ClientConnected(ConsumingPipe<CotChannelData> next) {
            super(true, next);
        }

        @Override
        public CotChannelData process(CotChannelData input) {
            return input;
        }
    }

    public static class StartListening extends AbstractFunctionConsumingPipe<Void, Void> {

        public StartListening(ConsumingPipe<Void> next) {
            super(true, next);
        }

        @Override
        public Void process(Void input) {
            return input;
        }
    }

}
