package com.bbn.marti.immortals;


import mil.darpa.immortals.core.InputProviderInterface;
import com.bbn.marti.immortals.data.CotChannelData;
import com.bbn.marti.immortals.data.CotData;
import com.bbn.marti.immortals.net.CotChannel;
import com.bbn.marti.immortals.net.CotServerChannel;
import com.bbn.marti.service.SubmissionService;
import com.bbn.marti.service.SubscriptionManager;
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

    private final Map<CotChannel,Integer> cotChannelUidMap = new HashMap<>();

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

    public static class AddCotServerChannelToService implements InputProviderInterface<CotServerChannel> {

        private SubmissionServiceFunctionalUnit ss;

        protected AddCotServerChannelToService(SubmissionServiceFunctionalUnit ss) {
            this.ss = ss;
        }

        @Override
        public void handleData(CotServerChannel data) {
            data.sendCotFromRemoteToLocal().addSuccessor(ss.sendCotDataToService());
            data.clientConnected().addSuccessor(ss.addCotClientToService());
            ss.transportList.add(data);
            log.debug(" input: " + data.toString());
        }
    }

    public static class AddCotClientToService implements InputProviderInterface<CotChannelData> {

        private SubmissionServiceFunctionalUnit ss;

        AtomicInteger streamingUidGen = new AtomicInteger(1);

        protected AddCotClientToService(SubmissionServiceFunctionalUnit ss) {
            this.ss = ss;
        }

        @Override
        public void handleData(CotChannelData data) {
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

                data.cotChannel.sendCotFromRemoteToLocal().addSuccessor(ss.sendCotDataToService());
                CallsignExtractorDataReceivedCallback cedrc = new CallsignExtractorDataReceivedCallback(ss);
                data.cotChannel.sendCotFromRemoteToLocal().addSuccessor(cedrc);
                data.cotChannel.remoteDisconnected().addSuccessor(ss.removeCotClientFromService);
            }
        }
    }

    public static class RemoveCotClientFromService implements InputProviderInterface<CotChannel> {

        private SubmissionServiceFunctionalUnit ss;

        protected RemoveCotClientFromService(SubmissionServiceFunctionalUnit ss) {
            this.ss = ss;
        }

        @Override
        public void handleData(CotChannel data) {
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
    }

    public static class SendCotDataToService implements InputProviderInterface<CotData> {

        private SubmissionServiceFunctionalUnit ss;

        protected SendCotDataToService(SubmissionServiceFunctionalUnit ss) {
            this.ss = ss;
        }

        @Override
        public void handleData(CotData data) {
            //log.debug("ProtocolListener received CoT data over transport: " + transport);
            data.cotEventContainer.setContextValue(SOURCE_TRANSPORT_KEY, data.channel);
            ss.addToInputQueue(data.cotEventContainer);
            //log.debug("Got message from input: " + transport.toString());
        }
    }

    private static class CallsignExtractorDataReceivedCallback implements InputProviderInterface<CotData> {

        private SubmissionServiceFunctionalUnit ss;

        protected CallsignExtractorDataReceivedCallback(SubmissionServiceFunctionalUnit ss) {
            this.ss = ss;
        }

        @Override
        public void handleData(CotData data) {
            //log.info("Got message from : " + data.getUid());
            String endpoint = data.cotEventContainer.getEndpoint();
            if (endpoint != null) {
                // Set the callsign on the subscription to match the client's reported SA message
                ss.subMgr.setCallsignForSubscription(data.cotEventContainer.getCallsign(), data.channel);

                // Stop listening now that it's been set
//					protocol.removeDataReceiver(this);
            }
        }
    }
}
