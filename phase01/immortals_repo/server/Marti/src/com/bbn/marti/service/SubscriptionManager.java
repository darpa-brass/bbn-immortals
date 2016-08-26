package com.bbn.marti.service;

import com.bbn.cot.CotEventContainer;
import com.bbn.marti.immortals.data.CotChannelData;
import com.bbn.marti.immortals.net.CotChannel;
import com.bbn.marti.net.CotEventSender;
import com.bbn.marti.remote.RemoteSubscription;
import com.bbn.marti.remote.SubscriptionManagerInterface;
import org.apache.log4j.Logger;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class SubscriptionManager extends UnicastRemoteObject
        implements SubscriptionManagerInterface {

    private static final long serialVersionUID = -2282561167018194365L;
    public static String SubscriberHitsKey = "SUBSCRIBER_HITS_KEY";
    public static String SUBSCRIPTION_PREFIX = "subscription.static.";
    private static Logger log = Logger.getLogger(SubscriptionManager.class);
    Map<String, Subscription> subMap = new HashMap<String, Subscription>();
    int ttl;
    private Map<String, Subscription> callsignMap = new HashMap<String, Subscription>();

    public SubscriptionManager() throws RemoteException {
        super();
        Integer configTtl = CoreConfig.getInstance().getAttributeInteger("network.multicastTTL");
        ttl = 1;
        if (configTtl != null) {
            ttl = configTtl;
        }
    }

    public static boolean matchesXPath(CotEventContainer c, String xpath) {
        return (xpath == null // xpath is null
                || xpath.trim().length() < 1    // or xpath is empty
                || c.matchXPath(xpath)); // or the xpath expression actually matches
    }

    synchronized List<CotEventContainer> getPersistedSubs() {
        // TODO: IMMORTALS: REENABLE
//        Boolean reloadSubs = (Boolean) CoreConfig.getInstance().getAttribute("subscription.reloadPersistent");
//        if (reloadSubs == null || reloadSubs.booleanValue() != true ||
//                CoreConfig.getInstance().getAttributeBoolean(RepositoryService.ENABLED_CONFIG_KEY) != true) {
        return null;
//        }
//
//        return RepositoryService.getAllSubs();
    }

    /**
     * Returns the list of subscriptions that this message is going to.
     * <p>
     * The message is passed through every subscription's xpath, and only those subscriptions with matching xpaths are
     * returned.
     */
    List<Subscription> getMatches(CotEventContainer c) {
        List<Subscription> rval = new LinkedList<Subscription>();

        // for each subscription
        for (Subscription s : subMap.values()) {
            //log.debug("Sub: " + s.uid + ", " + s.sender.getTransport());

            // if xpath is null
            if (matchesXPath(c, s.xpath)) {
                // don't send messages back over the EXACT same
                //  transport stream from which they came.
                if (s.sender.getChannel() !=
                        c.getContextValue(SubmissionService.SOURCE_TRANSPORT_KEY)) {
                    // otherwise, add to match list
                    rval.add(s);
                } else {
                    // for now, log this event - at least until it's tested
//					log.debug("NOT sending message back over the same TCP stream from which it came: "
//							+ c.getContextValue(SubmissionService.SOURCE_TRANSPORT_KEY) + ", uid:" + s.uid);
                }
            } else {
                //log.debug("NOT matching subscription for uid: " + s.uid);
            }
        }
        return rval;
    }

    synchronized private void _addSubscription(Subscription s) {
        subMap.put(s.uid, s);
        Thread senderThread = new Thread(s.sender);
        senderThread.setName("Sender:" + s.uid);
        senderThread.start();
        // TODO: IMMORTALS: REENABLE
//        if (CoreConfig.getInstance().getAttributeBoolean(RepositoryService.ENABLED_CONFIG_KEY))
//            RepositoryService.persistSubscription(s);

        log.info("Added Subscription: " + s);
    }

    @Override
    synchronized public boolean deleteSubscription(String uid) throws RemoteException {
        // TODO: IMMORTALS: REENABLE
//        if (CoreConfig.getInstance().getAttributeBoolean(RepositoryService.ENABLED_CONFIG_KEY))
//            RepositoryService.unpersistSubscription(uid);
        Subscription sub = subMap.get(uid);
        sub.sender.stopThread();
        log.info("Removed Subscription: " + uid);
        for (Map.Entry<String, Subscription> e : callsignMap.entrySet()) {
            if (e.getValue().uid.compareTo(uid) == 0) {
                callsignMap.remove(e.getKey());
                break;
            }
        }
        return (subMap.remove(uid) != null);
    }

    synchronized boolean removeSubscription(CotChannelData c) throws RemoteException {
        for (Subscription s : subMap.values()) {
            if (s.sender.getChannel() == c.cotChannel) {
                return deleteSubscription(s.uid);
            }
        }
        return false;
    }

    public void addSubscription(String uid, CotChannelData channel, String xpath, boolean proxy) throws RemoteException {
        addSubscription(uid, channel, xpath, null, proxy);
    }

    public void addSubscription(String uid, CotChannelData channel,
                                String xpath, String xml, boolean proxy) throws RemoteException {
        Subscription s = new Subscription();
        s.uid = uid;
        s.xpath = xpath;
        s.sender = new CotEventSender(channel);
        s.to = channel.toString();
        s.rawXml = xml;
        s.proxy = proxy;
        _addSubscription(s);
    }

    @Override
    public synchronized ArrayList<RemoteSubscription> getSubscriptionList() throws RemoteException {
        ArrayList<RemoteSubscription> ret = new ArrayList<RemoteSubscription>(subMap.size());
        for (Subscription s : subMap.values()) {
            synchronized (s) {
                RemoteSubscription rs = new RemoteSubscription(s);
                rs.currentBandwidth = s.sender.getBandwidthLimitKbps();
                rs.currentQueueDepth = s.sender.queueSize();
                ret.add(rs);
            }
        }
        return ret;
    }

    public String getXpathForUid(String uid) throws RemoteException {
        Subscription s = subMap.get(uid);
        String xpath = s.xpath;
        if (xpath == null) {
            xpath = "";
        }
        return xpath;
    }

    @Override
    public void setXpathForUid(String uid, String xpath) throws RemoteException {
        Subscription s = subMap.get(uid);
        if (s != null) {
            s.xpath = xpath;
        } else {
            throw new RuntimeException("Invalid subscription uid: " + uid);
        }

    }

    synchronized public void setCallsignForSubscription(String callsign, CotChannel channel) {
        for (Subscription s : subMap.values()) {
            if (s.sender.getChannel() == channel) {
                callsignMap.put(callsign, s);
                s.notes = callsign;
                log.info("  Set callsign for subscription: " + s.uid + " to " + callsign);
                break;
            }
        }
    }

    synchronized public Subscription getSubscriptionByCallsign(String callsign) {
        return callsignMap.get(callsign);
    }

}
