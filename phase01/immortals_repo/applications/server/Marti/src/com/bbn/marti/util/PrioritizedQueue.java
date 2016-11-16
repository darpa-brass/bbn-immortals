package com.bbn.marti.util;

import com.bbn.marti.remote.QueueMetric;
import com.bbn.marti.service.CoreConfig;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/*
 * Revisions: $Log: MasterPublicationQueue.java,v $
 */
public class PrioritizedQueue<E> {
    static final long DEFAULT_MAX_QUEUE_SIZE_IN_BYTES = 500000000;
    static final int DEFAULT_PER_PRIORITY_CAPACITY_IN_ELEMENTS = 20;
    static final int DEFAULT_NUM_IMPORTANCE_LEVELS = 1;
    private static Logger log = Logger.getLogger(PrioritizedQueue.class
            .getSimpleName());
    private final Semaphore available = new Semaphore(0, false);
    ArrayList<LinkedList<QueueElement<E>>> prioQ;

    // keep track of each importance level's usage
    QueueMetric[] queueMetrics;
    Random random = new Random();
    String name = "";
    public PrioritizedQueue() {
        Integer configPriorityLevels =
                CoreConfig.getInstance().getAttributeInteger(CoreConfig.QUEUE_PRIORITYLEVELS_KEY);
        if (configPriorityLevels == null)
            configPriorityLevels = DEFAULT_NUM_IMPORTANCE_LEVELS;

        Integer configCapacity =
                CoreConfig.getInstance().getAttributeInteger(CoreConfig.QUEUE_CAPACITY_KEY);
        if (configCapacity == null)
            configCapacity = DEFAULT_PER_PRIORITY_CAPACITY_IN_ELEMENTS;


        this.init(configPriorityLevels, configCapacity);
    }

    public PrioritizedQueue(int nImportanceLevels, int capacityPerLevel) {
        this.init(nImportanceLevels, capacityPerLevel);
    }

    protected void init(int nImportanceLevels, int capacityPerLevel) {
        this.prioQ = new ArrayList<LinkedList<QueueElement<E>>>(nImportanceLevels);
        this.queueMetrics = new QueueMetric[nImportanceLevels];
        for (int i = 0; i < nImportanceLevels; ++i) {
            this.prioQ.add(i, new LinkedList<QueueElement<E>>()); // already appends to the end
            this.queueMetrics[i] = new QueueMetric();
            this.queueMetrics[i].capacity.set(capacityPerLevel);
        }
    }

    synchronized public boolean addItem(E element, int importance, String key, boolean replaceByPubUID) {
        // IMMORTALS
        importance = 0;

        QueueElement<E> qe = new QueueElement<E>(element, key);
        boolean success = false;
        if (replaceByPubUID) {
            synchronized (this.prioQ.get(importance)) {
                for (QueueElement<E> i : this.prioQ.get(importance)) {
                    if (i.compareTo(qe) == 0) {
                        //log.debug("Found a match for key: " + qe.key + "! Replacing.");
                        i.element.set(qe.element.get());
                        return true;
                    }
                }
            }
        }

        if (this.queueMetrics[importance].currentSize.get() <
                this.queueMetrics[importance].capacity.get()) {
            this.queueMetrics[importance].currentSize.incrementAndGet();
            synchronized (this.prioQ.get(importance)) {
                if (this.prioQ.get(importance).add(qe)) {  // there are certain ways this could fail
                    this.available.release();
                    success = true;
                }
            }
        } else {
            // no space left in queue...
            log.error("Overfull queue for importance level: " + importance + "; " +
                    this.getNumItems() + " items in queue; " +
                    this.queueMetrics[importance].currentSize.get() + " elements out of " +
                    this.queueMetrics[importance].capacity.get());
            success = false;
        }

        return success;
    }

    public void destroy() {
        //log.error("Destroy called!");
        this.available.drainPermits();
        for (int i = 0; i < prioQ.size(); ++i) {
            prioQ.get(i).clear();
        }
        this.prioQ.clear();
    }

    public E getNextItemStrictPriority() throws InterruptedException {
        QueueElement<E> rval = null;
        this.available.acquire();
        for (int i = 0; i < prioQ.size(); ++i) {
            synchronized (prioQ.get(i)) {
                rval = prioQ.get(i).poll();
            }
            if (rval != null) {
                this.queueMetrics[i].currentSize.addAndGet(-1);
                break;
            }
        }
        return rval.element.get();
    }

    public List<E> drain(int maxNumber) {
        LinkedList<E> rval = new LinkedList<E>();
        // This prevents a busy-loop by waiting until
        // we receive an object to drain any from the queue.
        // Make sure to put it back (i.e., release) prior to starting execution
        // logic to keep the count accurate.
        try {
            this.available.acquire();
            this.available.release();
        } catch (InterruptedException ie) {
            return rval;
        }

        // Now pull everything out of the queue (up to maxNumber)
        for (int i = 0; i < prioQ.size(); ++i) {
            synchronized (prioQ.get(i)) {
                QueueElement<E> next = null;
                while ((next = prioQ.get(i).poll()) != null) {
                    rval.add(next.element.get());
                    try {
                        this.available.acquire();
                    } catch (InterruptedException e) {
                    }
                    this.queueMetrics[i].currentSize.addAndGet(-1);
                    if (maxNumber > 0 && rval.size() >= maxNumber)
                        return rval;
                }
            }
        }
        return rval;
    }

    /**
     * Returns a list of elements, waiting at most maxMillis for maxNumber to become available.
     * <p>
     * If maxNumber becomes available within the time window, that many are returned. Otherwise,
     * the method blocks until at least one becomes available, and then returns as many as are available.
     */
    public List<E> drain(int maxNumber, long maxMillis) {
        try {
            if (this.available.tryAcquire(maxNumber, maxMillis, TimeUnit.MILLISECONDS)) {
                // put maxNumber of permits back
                this.available.release(maxNumber);
            }
            return drain(maxNumber);
        } catch (InterruptedException e) {
            return new LinkedList<E>();
        }
    }

    /**
     * Returns the index of the first nonempty queue, or -1 if none can be found
     */
    private int getNonemptyQueue(int startIndex) {
        for (int i = startIndex; i < prioQ.size(); i++) {
            LinkedList<QueueElement<E>> finger = prioQ.get(i);
            synchronized (finger) {
                if (finger.size() > 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    private boolean flipCoin() {
        return random.nextInt(100) < 50;
    }

    public int getNumItems() {
        return this.available.availablePermits();
    }

    synchronized public long getNumItems(int priority) {
        if (prioQ.get(priority) != null) {
            return prioQ.get(priority).size();
        } else {
            return 0;
        }
    }

    public int getNumImportanceLevels() {
        return this.prioQ.size();
    }

    public String queueStatus() {
        StringBuilder rval = new StringBuilder();
        for (int i = 0; i < prioQ.size(); ++i) {
            rval.append(this.prioQ.get(i).size());
            rval.append(", ");
        }

        return rval.toString();
    }

    public String getName() {
        return name;
    }

    public PrioritizedQueue<E> setName(String name) {
        this.name = name;
        return this;
    }

    public QueueMetric getQueueMetric(int importanceLevel) {
        if (importanceLevel >= 0 && importanceLevel < this.getNumImportanceLevels())
            return this.queueMetrics[importanceLevel];
        return null;
    }

    // SIMON: element reference is atomic, but the key is not?
    static class QueueElement<E> implements Comparable<QueueElement<E>> {
        public AtomicReference<E> element = new AtomicReference<E>();
        public String key;  //for replacement

        QueueElement(E e, String key) {
            this.element.set(e);
            this.key = key;
        }

        @Override
        public int compareTo(QueueElement<E> o) {
            return key.compareTo(o.key);
        }

    }
}
