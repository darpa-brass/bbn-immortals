package com.bbn.marti.immortals;


import com.bbn.marti.immortals.data.CotChannelData;
import com.bbn.marti.immortals.data.CotData;
import com.bbn.marti.immortals.net.CotChannel;
import com.bbn.marti.immortals.net.CotServerChannel;
import com.bbn.marti.service.SubmissionService;
import com.bbn.marti.service.SubscriptionManager;
import mil.darpa.immortals.core.InputProviderInterface;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * I created this to isloate the core functionality of MartiRouter from the interfaces used by the code using the proper pipeline architecture
 * Created by awellman@bbn.com on 1/11/16.
 */

public class SubmissionServiceFunctionalUnit extends SubmissionService {

    private static Logger log = Logger.getLogger(SubmissionServiceFunctionalUnit.class);

    private final SubscriptionManager subMgr;

    private final AddCotClientToService addCotClientToService;
    private final AddCotServerChannelToService addCotServerChannelToService;
    private final SendCotDataToService sendCotDataToService;
    private final RemoveCotClientFromService removeCotClientFromService;

    private final Map<CotChannel, Integer> cotChannelUidMap = new HashMap<>();

    public AddCotClientToService addCotClientToService() {
        return addCotClientToService;
    }

    public AddCotServerChannelToService addCotServerChannelToService() {
        return addCotServerChannelToService;
    }

    public SendCotDataToService sendCotDataToService() {
        return sendCotDataToService;
    }

    public SubmissionServiceFunctionalUnit(final SubscriptionManager subMgr) {
        super(subMgr);
        this.subMgr = subMgr;

        addCotClientToService = new AddCotClientToService(this);
        addCotServerChannelToService = new AddCotServerChannelToService(this);
        sendCotDataToService = new SendCotDataToService(this);
        removeCotClientFromService = new RemoveCotClientFromService(this);
    }

    public static class AddCotServerChannelToService implements ConsumingPipe<CotServerChannel> {

        private SubmissionServiceFunctionalUnit ss;

        protected AddCotServerChannelToService(SubmissionServiceFunctionalUnit ss) {
            this.ss = ss;
        }

        @Override
        public void consume(CotServerChannel csc) {
            csc.sendCotFromRemoteToLocal().addNext(ss.sendCotDataToService());
            csc.clientConnected().setNext(ss.addCotClientToService());
            ss.transportList.add(csc);
            log.debug(" input: " + csc.toString());
        }

        @Override
        public void flushPipe() {

        }

        @Override
        public void closePipe() {

        }
    }

    public static class AddCotClientToService implements ConsumingPipe<CotChannelData> {

        private SubmissionServiceFunctionalUnit ss;

        AtomicInteger streamingUidGen = new AtomicInteger(1);

        protected AddCotClientToService(SubmissionServiceFunctionalUnit ss) {
            this.ss = ss;
        }

        @Override
        public void consume(CotChannelData data) {
            // Add to subscribers

            synchronized (ss) {
                try {
                    boolean proxy = true;
                    int uid = streamingUidGen.getAndIncrement();
                    ss.cotChannelUidMap.put(data.cotChannel, uid);
                    ss.subMgr.addSubscription(Integer.toString(uid)/* uid */,
                            data,
                            null /* null xpath means receiver gets everything */,
                            proxy);
                } catch (RemoteException e) {
                    log.warn("Remote exception adding subscription: " + data, e);
                }

                data.cotChannel.sendCotFromRemoteToLocal().addNext(ss.sendCotDataToService());
                CallsignExtractorDataReceivedCallback cedrc = new CallsignExtractorDataReceivedCallback(ss);
                data.cotChannel.sendCotFromRemoteToLocal().addNext(cedrc);
                data.cotChannel.remoteDisconnected().setNext(ss.removeCotClientFromService);
            }
        }

        @Override
        public void flushPipe() {
        }

        @Override
        public void closePipe() {
        }
    }

    public static class RemoveCotClientFromService implements ConsumingPipe<CotChannel> {

        private SubmissionServiceFunctionalUnit ss;

        protected RemoveCotClientFromService(SubmissionServiceFunctionalUnit ss) {
            this.ss = ss;
        }

        @Override
        public void consume(CotChannel data) {
            synchronized (ss) {
                if (ss.cotChannelUidMap.containsKey(data)) {
                    int uid = ss.cotChannelUidMap.get(data);
                    ss.cotChannelUidMap.remove(data);
                    try {
                        ss.subMgr.deleteSubscription(Integer.toString(uid));
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("RemoteDisconnected!!");

                } else {
                    System.err.println("No Uid exists in SubmissionServiceFunctionalUnit for provided CotChannel!");
                }
            }
        }

        @Override
        public void flushPipe() {
        }

        @Override
        public void closePipe() {
        }
    }

    public static class SendCotDataToService implements ConsumingPipe<CotData> {

        private SubmissionServiceFunctionalUnit ss;

        protected SendCotDataToService(SubmissionServiceFunctionalUnit ss) {
            this.ss = ss;
        }

        @Override
        public void consume(CotData cotData) {
            //log.debug("ProtocolListener received CoT data over transport: " + transport);
            cotData.cotEventContainer.setContextValue(SOURCE_TRANSPORT_KEY, cotData.channel);
            ss.addToInputQueue(cotData.cotEventContainer);
            //log.debug("Got message from input: " + transport.toString());
        }

        @Override
        public void flushPipe() {
        }

        @Override
        public void closePipe() {
        }
    }

    private static class CallsignExtractorDataReceivedCallback implements ConsumingPipe<CotData> {

        private SubmissionServiceFunctionalUnit ss;

        protected CallsignExtractorDataReceivedCallback(SubmissionServiceFunctionalUnit ss) {
            this.ss = ss;
        }

        @Override
        public void consume(CotData cotData) {
            //log.info("Got message from : " + data.getUid());
            String endpoint = cotData.cotEventContainer.getEndpoint();
            if (endpoint != null) {
                // Set the callsign on the subscription to match the client's reported SA message
                ss.subMgr.setCallsignForSubscription(cotData.cotEventContainer.getCallsign(), cotData.channel);

                // Stop listening now that it's been set
//					protocol.removeDataReceiver(this);
            }
        }

        @Override
        public void flushPipe() {
        }

        @Override
        public void closePipe() {
        }
    }
}
