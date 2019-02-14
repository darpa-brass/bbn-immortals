package com.bbn.marti.service;

import com.bbn.cot.CotEventContainer;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseService {
    private static Logger log = Logger.getLogger(BaseService.class
            .getSimpleName());

    protected AtomicBoolean keepGoing = new AtomicBoolean(false);
    protected Thread runThread = null;
    protected List<BaseService> consumers = new LinkedList<BaseService>();

    public void startService() {
        //LinkedList<BadgeOfShame> badMessages =
        //    (LinkedList<BadgeOfShame>) CoreMonitor.getInstance().getAttribute(BadgeOfShame.SHAMEWALL_KEY);;

        log.debug("Starting " + name());

        keepGoing.set(true);
        runThread = new Thread(new Runnable() {
            public void run() {
                while (keepGoing.get()) {
                    try {
                        processNextEvent();
                    } catch (Exception e) {
                        log.error("Error processing event: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });
        runThread.setName("Service:" + this.name());
        runThread.start();
    }

    public void stopService(boolean wait) {
        if (runThread == null)
            return;
        keepGoing.set(false);
        runThread.interrupt();
        if (wait) {
            try {
                runThread.join();
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for " + this.name()
                        + " service to stop.");
            }
        }
    }

    // TODO: add a filter argument so that consumers can be selective
    public void addConsumer(BaseService b) {
        consumers.add(b);
    }

    abstract public boolean hasRoomInQueueFor(CotEventContainer c);

    abstract public String name();

    abstract public boolean addToInputQueue(CotEventContainer c);

    abstract protected void processNextEvent();
}
