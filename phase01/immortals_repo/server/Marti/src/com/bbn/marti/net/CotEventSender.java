package com.bbn.marti.net;

import com.bbn.cot.CotEventContainer;
import com.bbn.marti.immortals.data.CotChannelData;
import com.bbn.marti.immortals.data.CotData;
import com.bbn.marti.immortals.net.CotChannel;
import com.bbn.marti.service.CoreConfig;
import com.bbn.marti.service.CoreMonitor;
import com.bbn.marti.util.PrioritizedQueue;
import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CotEventSender implements Runnable {

    protected static final int UNLIMITED_BW = -1;
    protected static final int MAXIMUM_BACKOFFS_MILLIS = 60000; // 1 minute
    private static Logger log = Logger.getLogger(CotEventSender.class.getSimpleName());
    CotChannel cotChannel;
    AtomicInteger availBwKbps = new AtomicInteger(UNLIMITED_BW);
    AtomicBoolean keepgoing = new AtomicBoolean(true);
    Thread runThread = null;
    AtomicBoolean shouldUseDisruptionTolerance = new AtomicBoolean(true);

    PrioritizedQueue<CotEventContainer> q = new PrioritizedQueue<CotEventContainer>();
    int nEventSent = 0;
    AtomicInteger periodicOutputInBytes = new AtomicInteger(0);
    // Used to limit the frequency of logging error messages
    private long lastTimePrinted = 0;

    public CotEventSender(CotChannelData cotChannelData) {
        this.cotChannel = cotChannelData.cotChannel;
        q.setName("CotEventSender-" + cotChannelData.host.getHostAddress() + ":" + cotChannelData.port);
        for (int i = 0; i < q.getNumImportanceLevels(); ++i)
            CoreMonitor.getInstance().setAttribute(
                    q.getName() + ".inputqueue." + i,
                    q.getQueueMetric(i));

        // Set it once at creation-time so that we don't incur a penalty for
        // checking the setting at every send.
        // TODO: we should add a way to register for changes to a setting in
        // CoreConfig and have it inform us if/when it's altered at runtime
        if (CoreConfig.getInstance().getAttributeBoolean("dissemination.smartRetry") == null)
            shouldUseDisruptionTolerance.set(false);
        else
            shouldUseDisruptionTolerance.set(
                    CoreConfig.getInstance()
                            .getAttributeBoolean("dissemination.smartRetry"));
    }

    public void addToOutputQueue(CotEventContainer c, int importance, boolean replaceByKey) {
        //log.debug("adding item to output queue");
        q.addItem(c, importance, c.getUid(), replaceByKey);
    }

    public CotChannel getChannel() {
        return cotChannel;
    }

    /**
     * Sends the next item off the queue; if an error occurs, it is logged, but
     * otherwise ignored.
     *
     * @return number of bytes written or -1 if error
     */
    private int sendNextElementOffQueue() throws InterruptedException {
        int nbytes = -1;

        // pop the next item off the queue
        CotEventContainer qElement = q.getNextItemStrictPriority();

        // if it was NULL, return...
        if (qElement == null)
            return 0; // no bytes written

        try {
            // try to send it
            // TODO: Austin: Is this right? Are there cases where nbytes will not equal this and would need to be obtained from sendingCotData?
            nbytes = qElement.asXml().getBytes().length;
            cotChannel.sendCotFromLocalToRemote().handleData(new CotData(qElement, cotChannel));
        } catch (Exception e) {
            // indicate a send error
            nbytes = -1;

            // Only print an error message at most every ten seconds
            if (System.currentTimeMillis() - lastTimePrinted > 10000) {
                log.error("Problem sending msg to " + cotChannel + ": " + e.getMessage());
                lastTimePrinted = System.currentTimeMillis();
            }
            throw new RuntimeException(e);
        }
        return nbytes;
    }

    @Override
    public void run() {
        synchronized (this) {
            this.runThread = Thread.currentThread();
            //System.err.println("Starting sending thread");
            keepgoing.set(true);
        }
        // handle special case of being initialized with zero bandwidth
        if (this.availBwKbps.get() == 0) {
            sleepUntilNext(0, 0);
        }

        while (keepgoing.get()) {
            // Store the time before we started sending data over the wire
            long startTime = System.currentTimeMillis();
            try {
                // Send the next item; get number of bytes written
                int nbytes = sendNextElementOffQueue();

                // Backoff depending on my QoS bandwidth allowance
                if (nbytes > 0) {
                    this.periodicOutputInBytes.addAndGet(nbytes);
                    long delta = System.currentTimeMillis() - startTime;
                    sleepUntilNext(nbytes, delta);
                }
            } catch (InterruptedException e) {  // ignore
            }
        }
        synchronized (this) {
            this.runThread = null;
        }
        // Remove the queue in CoreMonitor
        CoreMonitor.getInstance().removeAttribute(q.getName() + ".inputqueue");
        for (int i = 0; i < q.getNumImportanceLevels(); ++i)
            CoreMonitor.getInstance().removeAttribute(q.getName() + ".inputqueue." + i);
    }

    public void stopThread() {
        synchronized (this) {
            keepgoing.set(false);
            if (this.runThread != null) {
                runThread.interrupt();
            }
        }
    }

    private void sleepUntilNext(long messageSize, long deltaMs) {
        long oldBw = availBwKbps.get();
        long nextTimeMs;
        if (oldBw == UNLIMITED_BW) {
            return;
        }
        if (oldBw != 0) {
            nextTimeMs = ((1000 * messageSize * 8) / 1024) / oldBw;
            if (deltaMs >= nextTimeMs) {
                // this warning only makes sense if we know the queue will always have something in it
                //System.err.println("Warning, sending too slow!  Bandwidth must be lower than expected.");
                return;
            } else {
                nextTimeMs -= deltaMs;
            }
        } else {
            nextTimeMs = 0;
            System.err.println("Warning: zero bandwidth available.  Waiting for update...");
        }

        long nextTimeAbs = System.currentTimeMillis() + nextTimeMs;

        try {
            //System.err.println("bw: " + oldBw);
            //System.err.println("Sleeping " + nextTimeMs + "ms for message of size: " + (messageSize* 8) / 1024 + " kbits");
            this.wait(nextTimeMs);

        } catch (InterruptedException e) {
            long remainingTime = nextTimeAbs - System.currentTimeMillis();
            long nextSize = (remainingTime * oldBw * 1024) / 8 / 1000;
            //System.err.println("Interupted sleep! old size: " + (messageSize* 8) / 1024 + ", new size: " + (nextSize* 8) / 1024);
            if (nextSize <= 0) {
                // the new bandwidth is higher than our old one, and we can send immediately
                return;
            }
            sleepUntilNext(nextSize, 0);
        }
    }

    public int getAndResetPeriodicBytes() {
        int r = this.periodicOutputInBytes.get();
        this.periodicOutputInBytes.set(0);
        return r;
    }

    public int getBandwidthLimitKbps() {
        return availBwKbps.get();
    }

    public void setBandwidthLimitKbps(int v) {
        availBwKbps.set(v);
    }

    public int queueSize() {
        return q.getNumItems();
    }
}
