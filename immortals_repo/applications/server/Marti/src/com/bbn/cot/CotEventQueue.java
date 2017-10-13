package com.bbn.cot;

import org.apache.log4j.Logger;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Matt wanted to make this class, but I needed it, so it's just a shell for
 * now.
 *
 * @author kusbeck
 */
public class CotEventQueue extends LinkedBlockingQueue<CotEventContainer> {
    private static final long serialVersionUID = 8668879729240362877L;

    private static Logger log = Logger.getLogger(CotEventQueue.class
            .getSimpleName());

    private int pollTimeoutSec = 30;

    public CotEventContainer blockingPoll() {
        try {
            return this.poll(pollTimeoutSec, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.warn("Queue poll interrupted", e);
        }
        return null;
    }
}
