package com.bbn.marti.util;

import com.bbn.marti.remote.QueueMetric;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class FixedSizedBlockingQueue<E> {
    private static final long serialVersionUID = -6588813573996771755L;
    public QueueMetric queueMetric = new QueueMetric();
    private LinkedBlockingQueue<E> queue = new LinkedBlockingQueue<E>();

    public FixedSizedBlockingQueue<E> setSizeLimit(int size) {
        queueMetric.capacity.set(size);
        return this;
    }

    public boolean add(E toAdd) {
        if (queueMetric.currentSize.getAndIncrement() < queueMetric.capacity.get()) {
            return queue.add(toAdd);
        } else {
            // we incremented atomically, but aren't adding, so put the counter back where it was
            queueMetric.currentSize.decrementAndGet();
        }
        // else, drop it!
        return false;
    }

    public E take() throws InterruptedException {
        E toReturn = queue.take();
        queueMetric.currentSize.decrementAndGet();
        return toReturn;
    }

    public List<E> take(int maxNumToTake) throws InterruptedException {
        List<E> ret = new LinkedList<E>();
        ret.add(queue.take()); // blocks
        int numTaken = 1;
        numTaken += queue.drainTo(ret, maxNumToTake - 1);
        queueMetric.currentSize.addAndGet(-1 * numTaken);
        return ret;
    }

    public QueueMetric getQueueMetrics() {
        return queueMetric;
    }
}
