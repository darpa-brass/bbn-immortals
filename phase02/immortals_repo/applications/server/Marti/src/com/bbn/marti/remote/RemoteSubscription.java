package com.bbn.marti.remote;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class RemoteSubscription implements Serializable {
    private static final long serialVersionUID = 8134731508521872646L;
    public String uid = null;
    public String to = null;
    public String rawXml = null; //XXX: Hack!
    public String xpath = null;
    public ImagePref imgPref = ImagePref.THUMBNAIL;
    public AtomicBoolean suspended = new AtomicBoolean(false);
    public AtomicInteger numHits = new AtomicInteger(0);
    public AtomicLong lastProcTime = new AtomicLong(0L);
    public String notes = null;
    // QoS attributes; need to be collected on-demand from external components
    public String mode;
    public int currentBandwidth;
    public int currentQueueDepth;
    public RemoteSubscription(RemoteSubscription toCopy) {
        this.uid = toCopy.uid;
        this.to = toCopy.to;
        this.rawXml = toCopy.rawXml;
        this.xpath = toCopy.xpath;
        this.imgPref = toCopy.imgPref;
        this.suspended = toCopy.suspended;
        this.numHits = toCopy.numHits;
        this.lastProcTime = toCopy.lastProcTime;
        this.notes = toCopy.notes;

        this.mode = toCopy.mode;
        this.currentBandwidth = toCopy.currentBandwidth;
        this.currentQueueDepth = toCopy.currentQueueDepth;
    }
    // End QoS attributes

    public RemoteSubscription() {
    }

    public void incHit(long curTime) {
        lastProcTime.set(curTime);
        numHits.incrementAndGet();
    }

    public String toString() {
        return "uid: " + uid + "; to: " + to + "; xpath: " + xpath;
    }

    public enum ImagePref {
        FULL_IMAGE,
        THUMBNAIL,
        URL_ONLY,
        DATABASE,
        NONE
    }
}


