package com.bbn.marti.service;

import com.bbn.cot.CotEventContainer;
import com.bbn.filter.ImageFormattingFilter;
import com.bbn.marti.remote.QueueMetric;
import com.bbn.marti.remote.RemoteSubscription.ImagePref;
import com.bbn.marti.util.PrioritizedQueue;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BrokerService extends BaseService {
    private static Logger log = Logger.getLogger(BrokerService.class);
    ExecutorService execService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    SubscriptionManager subMgr;
    PrioritizedQueue<CotEventContainer> inputQueue =
            new PrioritizedQueue<CotEventContainer>().setName("Broker");

    public BrokerService(SubscriptionManager subMgr) {
        this.subMgr = subMgr;
        for (int i = 0; i < inputQueue.getNumImportanceLevels(); ++i)
            CoreMonitor.getInstance().setAttribute(
                    inputQueue.getName() + ".inputqueue." + i,
                    inputQueue.getQueueMetric(i));
    }

    protected static void processMessage(CotEventContainer cot) {
        //log.debug("processing event: " + cot.getDocument().asXML());
        long hitTime = System.currentTimeMillis();

        List<Subscription> hits = (List<Subscription>) cot.getContextValue(SubscriptionManager.SubscriberHitsKey);

        if (hasImage(cot)) {
            // initialize work conserving structures
            int prefEnumCount = ImagePref.values().length;
            CotEventContainer[] prefFormats = new CotEventContainer[ImagePref.values().length];

            for (Subscription subscr : hits) {
                //log.debug("QoS mode for sub: " + s.uid + ": " + qosSpec.name);
                subscr.incHit(hitTime);
                ImagePref pref = subscr.imgPref;

                // get format for this message
                CotEventContainer toSend = getOrInitFormat(prefFormats, cot, pref);

                // add to output queue for subscription
                subscr.sender.addToOutputQueue(toSend, 0, false);
            }
        } else {
            for (Subscription subscr : hits) {
                subscr.incHit(hitTime);
//				ImportanceSpec qosSpec = subscr.lqm.getImportance(cot.getDocument());
                subscr.sender.addToOutputQueue(cot, 0, false);
            }

        }
    }

    /**
     * Returns whether the image field in the cot is uninitialized
     */
    private static boolean hasImage(CotEventContainer cot) {
        return cot.getContext(CotEventContainer.IMAGE_KEY) != null;
    }

    /**
     * Returns a Cot message formatted with the given image preference. If the format is preexisiting, it returns
     * a reference to the seminal message for that format. Otherwise, it creates and stores it into the given array,
     * in the enumeration ordinal index.
     */
    private static CotEventContainer getOrInitFormat(CotEventContainer[] formats, CotEventContainer seed, ImagePref pref) {
        // get cot message out for preference index
        int formatIdx = pref.ordinal();
        CotEventContainer cot = formats[formatIdx];

        if (cot == null) {
            // need to initialize and store new format -- uninitialized array entry is null
            cot = seed.copy();
            formats[formatIdx] = ImageFormattingFilter.filter(cot, pref);
        }

        return cot;
    }

    // TODO: something smart
    protected static int findPriority(CotEventContainer c) {
        return 0;
    }

    @Override
    public String name() {
        return "Broker";
    }

    @Override
    public boolean addToInputQueue(final CotEventContainer c) {
        if (c != null) {
            execService.execute(new Runnable() {
                public void run() {
                    List<Subscription> hits = subMgr.getMatches(c);
                    c.setContextValue(SubscriptionManager.SubscriberHitsKey, hits);
                    //log.debug("queuing event: " + c.getDocument().asXML());
                    inputQueue.addItem(c, findPriority(c), c.getUid(), false);
                }
            });

            return true;
        } else {
            //log.debug("purposefully not brokering this event.");
        }
        return false;
    }

    @Override
    protected void processNextEvent() {
        //log.debug("waiting to process next event...");
        //TODO: add flag to switch between weighted fair and strict priority
        CotEventContainer c = null;
        try {
            c = inputQueue.getNextItemStrictPriority();
        } catch (InterruptedException e) {
            return;
        }

        processMessage(c);

        for (BaseService s : consumers) {
            try {
                // need independent copies of event for each path out
                s.addToInputQueue(c.copy());
            } catch (Exception e) {
                log.warn("Exception while processing queue " + inputQueue
                        + " element " + c, e);
            }
        }
    }

    @Override
    public boolean hasRoomInQueueFor(CotEventContainer c) {
        int importance = findPriority(c);
        QueueMetric a = inputQueue.getQueueMetric(importance);

        return inputQueue.getQueueMetric(importance).currentSize.get()
                < inputQueue.getQueueMetric(importance).capacity.get();
    }

    protected List<Subscription> extractBrokering(List<String> destList) {
        List<Subscription> rval = new LinkedList<Subscription>();

        return rval;
    }
}
