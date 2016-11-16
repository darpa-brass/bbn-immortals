package mil.darpa.immortals.das.sourcecomposer.generators.javaclasstypes;

import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 8/5/16.
 */
public abstract class AbstractDataGenerator<JavaType> implements ConsumingPipe<Object> {

    private final int dataTransferIntervalMS;
    private final int dataTransferBurstCount;
    private int transferredCount = 0;

    private long lastSendTime = -1;

    private final ConsumingPipe<JavaType> next;

//    public AbstractDataGenerator(int dataTransferIntervalMS, int dataTransferBurstCount) {
//        this.dataTransferIntervalMS = dataTransferIntervalMS;
//        this.dataTransferBurstCount = dataTransferBurstCount;
//        this.next = null;
//    }

    public AbstractDataGenerator(int dataTransferIntervalMS, int dataTransferBurstCount, @Nonnull ConsumingPipe<JavaType> next) {
        this.dataTransferIntervalMS = dataTransferIntervalMS;
        this.dataTransferBurstCount = dataTransferBurstCount;
        this.next = next;
    }

//    public void setNext(@Nonnull ConsumingPipe<JavaType> next) {
//        this.next = next;
//    }

    private final boolean hasMore() {
        return (transferredCount < dataTransferBurstCount);
    }

    public final void startConsumingPipeProduction() {
        if (next == null) {
            throw new RuntimeException("Cannot produce data for an undefined Consumer!");
        } else {
            while (hasMore()) {
                try {
                    Thread.sleep(dataTransferIntervalMS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                next.consume(controlledGenerate());
            }
        }
    }

    private final JavaType controlledGenerate() {
        if (hasMore()) {
            transferredCount++;
            return innerProduce();
        } else {
            return null;
        }
    }

    protected abstract JavaType innerProduce();

    @Override
    public void consume(Object input) {
        startConsumingPipeProduction();
    }

    @Override
    public void flushPipe() {
        if (next != null) {
            next.flushPipe();
        }
    }

    @Override
    public void closePipe() {
        if (next != null) {
            next.closePipe();
        }
    }

//    @Override
//    public JavaType produce() {
//        long delta = System.currentTimeMillis() - lastSendTime;
//
//        if (lastSendTime != -1 && delta < dataTransferIntervalMS) {
//            try {
//                Thread.sleep(delta);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        lastSendTime = System.currentTimeMillis();
//        return controlledGenerate();
//    }
//
//    @Override
//    public void closePipe() {
//        if (next != null) {
//            next.flushPipe();
//            next.closePipe();
//        }
//    }
//
//    @Override
//    public int getBufferSize() {
//        return 1;
//    }
}
