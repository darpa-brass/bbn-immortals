package mil.darpa.immortals.core.synthesis;

import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Allows directing a single input into multiple outputs while processing it (optionally)
 * <p>
 * Created by awellman@bbn.com on 7/26/17.
 */
public abstract class AbstractMultisuccessorConsumingPipe<InputType, OutputType> implements ConsumingPipe<InputType> {

    private final LinkedList<ConsumingPipe<OutputType>> nextList = new LinkedList<>();
    private final Lock listLock = new ReentrantLock();
    private final boolean distributeNull;

    public AbstractMultisuccessorConsumingPipe(boolean distributeNull, @Nullable ConsumingPipe<OutputType>... next) {
        this.distributeNull = distributeNull;
        if (next != null && next.length > 0 && next[0] != null) {
            Collections.addAll(this.nextList, next);
        }
    }

    public synchronized void addNext(@Nonnull ConsumingPipe<OutputType> next) {
        if (next == null) {
            throw new RuntimeException("Cannot Add null pipeline elements!");
        }
        listLock.lock();
        try {
            this.nextList.add(next);
        } finally {
            listLock.unlock();
        }
    }

    public synchronized void removeNext(@Nonnull ConsumingPipe<OutputType> next) {
        listLock.lock();
        try {
            if (this.nextList.contains(next)) {
                this.nextList.remove(next);
            }
        } finally {
            listLock.unlock();
        }
    }

    @Override
    public void consume(InputType input) {
        listLock.lock();
        try {
            OutputType rval = process(input);

            if (rval != null || distributeNull) {
                distributeOutput(rval);
            }
        } finally {
            listLock.unlock();
        }
    }

    protected void distributeOutput(OutputType output) {
        listLock.lock();
        try {
            for (ConsumingPipe<OutputType> handler : nextList) {
                handler.consume(output);
            }
        } finally {
            listLock.unlock();
        }
    }

    public abstract OutputType process(InputType input);

    @Override
    public void flushPipe() {
        listLock.lock();
        try {
            for (ConsumingPipe<OutputType> handler : nextList) {
                handler.flushPipe();
            }
        } finally {
            listLock.unlock();
        }
    }

    @Override
    public void closePipe() {
        listLock.lock();
        try {
            for (ConsumingPipe<OutputType> handler : nextList) {
                handler.closePipe();
            }
        } finally {
            listLock.unlock();
        }
    }
}
