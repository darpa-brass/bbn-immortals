package mil.darpa.immortals.core.synthesis;

import mil.darpa.immortals.core.synthesis.interfaces.ProducingPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

/**
 * Allows simple function-based implementation of a single part of a pipeline
 * Created by awellman@bbn.com on 6/21/16.
 */
public abstract class ObjectPipe<InputType, OutputType> implements ConsumingPipe<InputType>, ProducingPipe<OutputType> {
    private final ConsumingPipe<OutputType> next;
    private final ProducingPipe<InputType> previous;

    public ObjectPipe(ConsumingPipe<OutputType> next) {
        this.next = next;
        this.previous = null;
        StringBuilder sb;
    }

    public ObjectPipe(ProducingPipe<InputType> previous) {
        this.next = null;
        this.previous = previous;
    }

    public ObjectPipe() {
        this.next = null;
        this.previous = null;
    }

    @Override
    public synchronized OutputType produce() {
        InputType input = previous.produce();
        OutputType output = process(input);
        return output;
    }

    public final synchronized void consume(InputType input) {
        OutputType output = process(input);
        if (next != null) {
            next.consume(output);
        }
    }

    @Override
    public synchronized final void flushPipe() {
        OutputType output = null;
        try {
            output = flushToOutput();
        } finally {
            if (next != null) {
                if (output != null) {
                    next.consume(output);
                }
                next.flushPipe();
            }
        }
    }

    @Override
    public synchronized final void closePipe() {
        if (next != null) {
            flushPipe();
            try {
                preNextClose();
            } finally {
                next.closePipe();
            }

        } else if (previous != null) {
            previous.closePipe();
        }
    }

    protected abstract OutputType process(InputType input);

    protected abstract OutputType flushToOutput();

    protected abstract void preNextClose();
}
