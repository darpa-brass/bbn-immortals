package com.bbn.marti.service;

import com.bbn.cot.CotEventContainer;
import com.bbn.filter.DropTypeFilter;
import com.bbn.filter.FlowTagFilter;
import com.bbn.filter.ImageProcessingFilter;
import com.bbn.marti.immortals.data.CotChannelData;
import com.bbn.marti.immortals.data.CotData;
import com.bbn.marti.immortals.net.CotChannel;
import com.bbn.marti.immortals.net.CotServerChannel;
import com.bbn.marti.util.FixedSizedBlockingQueue;
import mil.darpa.immortals.core.InputProviderInterface;
import org.apache.log4j.Logger;
import org.dom4j.Node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class SubmissionService extends BaseService {
    public static final String NETCFG_INPUT_PREFIX = "network.input.";
    public static final String SOURCE_TRANSPORT_KEY = "source.transport";
    public static final String DEFAULT_FLOWTAG_KEY = "filter.flowtag.text";
    public static final String DEFAULT_FLOWTAG_TEXT = "marti";
    private static final int SUBMISSION_SERVICE_INPUT_QUEUE_SIZE = 500;
    static ImageProcessingFilter imageFilter = new ImageProcessingFilter();
    static FlowTagFilter flowTagFilter;
    // initialize control message types
    static Set<String> controlMsgTypes = new HashSet<String>(Arrays.asList(
            new String[]{
                    "t-b",
                    "t-b-a",
                    "t-b-c",
                    "t-b-q"
            }));
    private static Logger log = Logger.getLogger(SubmissionService.class);

    static {
        String flowtag = CoreConfig.getInstance().getAttributeString(DEFAULT_FLOWTAG_KEY);

        if (flowtag == null) {
            flowtag = DEFAULT_FLOWTAG_TEXT;
        }
        flowTagFilter = new FlowTagFilter(flowtag);
    }

    SubscriptionManager subMgr;
    FixedSizedBlockingQueue<CotEventContainer> inputQueue =
            new FixedSizedBlockingQueue<CotEventContainer>();
    protected LinkedList<CotServerChannel> transportList = new LinkedList<CotServerChannel>();

    DropTypeFilter dropFilter = new DropTypeFilter("u-d-p");

    public SubmissionService(final SubscriptionManager subMgr) {
        this.subMgr = subMgr;
        CoreConfig config = CoreConfig.getInstance();
        Integer queueSizeConfig = config.getAttributeInteger(CoreConfig.QUEUE_CAPACITY_KEY);
        inputQueue.setSizeLimit(queueSizeConfig != null ? queueSizeConfig :
                SUBMISSION_SERVICE_INPUT_QUEUE_SIZE);
        CoreMonitor.getInstance().setAttribute(this.name() + ".inputqueue", inputQueue.queueMetric);
        Integer configTtl = config.getAttributeInteger("network.multicastTTL");
        int ttl = 1;
        if (configTtl != null) {
            ttl = configTtl;
        }
    }

    public static FlowTagFilter getFlowTagFilter() {
        return flowTagFilter;
    }

    @Override
    public String name() {
        return "Submission";
    }

    @Override
    public void startService() {
        super.startService();

        List<CotEventContainer> subs = subMgr.getPersistedSubs();
        if (subs != null) {
            for (CotEventContainer i : subs) {
                try {
                    processControlMessage(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        for (CotServerChannel s : transportList) {
            s.getStartListening().consume(null);
        }
    }

    @Override
    public boolean addToInputQueue(CotEventContainer c) {
        //log.debug("Adding CoT to input queue with current size: " + inputQueue.size());
        return inputQueue.add(c);
    }

    @Override
    protected void processNextEvent() {
        CotEventContainer c = null;
        try {
            //log.debug("Waiting to take object from queue");
            c = inputQueue.take(); // blocking call!
            //log.debug("Got object from queue: " + c);
        } catch (InterruptedException e1) {
            log.warn("Exception taking object from queue " + inputQueue, e1);
        }

        if (c != null) {
            if (dropFilter.filter(c) == null) {
                return;
            }
            if (isControlMessage(c)) {
                try {
                    processControlMessage(c);
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // consume control messages
                // (i.e., do NOT send them to subscribers)
                return;
            }

            if (c.matchXPath("/event/detail/_flow-tags_[@" + flowTagFilter.getServerId() + "]")) {
                //we've already processed this message, throw it away
                log.error("Duplicate message!");
                return;
            }

            // add a flow tag (to show that Marti has processed the message)
            flowTagFilter.filter(c);

            // move the images out of the XML and into the context.
            // Note: it's important that this comes before the pkAssignFilter
            //  because the PrimaryKeyAssignmentFilter uses context values that
            //  are set by the image filter.
            imageFilter.filter(c);

            // assign a primary key for the DB (for the cot_router table and
            // cot_image/cot_thumbnail tables)
            // TODO: IMMORTALS: REENABLE
//			pkAssignFilter.filter(c);

            //TODO: wrap this in a configurable conditional...
            // The difference is that, with this logic in place,
            // we won't broker anything that would get dropped by the repository.
            // I'm not doing it right now because I don't want the
            // the conditional to impact performance. ~Kyle
            boolean allServicesHaveRoom = true;
            for (BaseService s : consumers)
                if (!s.hasRoomInQueueFor(c))
                    allServicesHaveRoom = false;

            if (allServicesHaveRoom)
                for (BaseService s : consumers)
                    s.addToInputQueue(c.copy());

        }
    }

    private boolean isControlMessage(CotEventContainer c) {
        Node event = c.getDocument().selectSingleNode("/event");
        String type = event.valueOf("@type");

        return type != null && controlMsgTypes.contains(type);
    }

    public void processControlMessage(CotEventContainer c) throws IOException {
        log.debug("processing control message for uid: " + c.getUid());
        Node event = c.getDocument().selectSingleNode("/event");
        String type = event.valueOf("@type");

        switch (type) {
            case "t-b":
                processSubscriptionMessage(c);
                break;
            default:
                log.debug("deleting subscription for uid: " + c.getUid());
                subMgr.deleteSubscription(c.getUid());
        }
    }

    private void processSubscriptionMessage(CotEventContainer msg) throws IOException {
        String xpath = msg.getDocument().valueOf("/event/detail/subscription/tests/@xpath");
        CotChannel channel = null;

        Node subNode = msg.getDocument().selectSingleNode("/event/detail/subscription");
        String connectStr = subNode.valueOf("@publish");
        String[] tokens = connectStr.split(":");

        if (tokens.length < 3) {
            log.error("Malformed subscription endpoint: " + connectStr);
            return;
        }

        String ip = tokens[1];
        int port = Integer.parseInt(tokens[2]);
        String protocolString = tokens[0].toLowerCase();

        if ("tcp".equals(protocolString)) {
            // TODO: IMMORTALS: REENABLE
//			transport = new TcpTransport().setEndpoint(ip, port);
//			protocol = new StandardCotProtocol();
        } else if ("udp".equals(protocolString)) {
            // TODO: IMMORTALS: REENABLE
//			transport = new UdpTransport().setEndpoint(ip, port);
//			protocol = new StreamingCotProtocol();
        }
        // Only do logic here if you want to require a subscription message for STCP
        else if ("stcp".equals(protocolString)) {
            channel = (CotChannel) msg.getContextValue(SOURCE_TRANSPORT_KEY);
            final String uid = msg.getUid();
        }

        // Subscriptions have been disabled
        // Drop any subscriptions that are trying to deliver results to my own listening ports.
        // We had that happen at an exercise, Feb. 2013
//        if (ip.compareTo("127.0.0.1") == 0) {
//            for (CotServerChannel server : transportList) {
//                if (port == server.getLocalPort()) {
//                    log.error("Invalid subscription endpoint: " + port + " is in use as a listening port!");
//                    log.error(msg.asXml());
//                    return;
//                }
//            }
//        }

        // NOW, Add the subscription...
        // ...unless, of course, the protocol or transport weren't set above
        //    (e.g., onConnect subscription add)
        if (channel != null) {
            log.debug("adding subscription for uid: " + msg.getUid());
            boolean proxy = false;
            CotChannelData cotChannelData = new CotChannelData(channel, InetAddress.getByName(ip), protocolString, port);
            subMgr.addSubscription(msg.getUid(), cotChannelData, xpath, msg.asXml(), proxy);
        }
    }

    @Override
    public boolean hasRoomInQueueFor(CotEventContainer c) {
        return true; // Not used for this service.
    }

    private class CallsignExtractorDataReceivedCallback implements InputProviderInterface<CotData> {

        @Override
        public void handleData(CotData data) {
            //log.info("Got message from : " + data.getUid());
            String endpoint = data.cotEventContainer.getEndpoint();
            if (endpoint != null) {
                // Set the callsign on the subscription to match the client's reported SA message
                subMgr.setCallsignForSubscription(data.cotEventContainer.getCallsign(), data.channel);

                // Stop listening now that it's been set
//					protocol.removeDataReceiver(this);
            }
        }
    }

}
