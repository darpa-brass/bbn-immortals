package mil.darpa.immortals.core.synthesis;

import mil.darpa.immortals.core.synthesis.interfaces.ReadableObjectPipeInterface;
import mil.darpa.immortals.core.synthesis.interfaces.WriteableObjectPipeInterface;

/**
 * Allows simple function-based implementation of a single part of a pipeline
 * Created by awellman@bbn.com on 6/21/16.
 */
public abstract class ObjectPipe<InputType, OutputType> implements WriteableObjectPipeInterface<InputType>, ReadableObjectPipeInterface<OutputType> {
    private final WriteableObjectPipeInterface<OutputType> next;
    private final ReadableObjectPipeInterface<InputType> previous;

    public ObjectPipe(WriteableObjectPipeInterface<OutputType> next) {
        this.next = next;
        this.previous = null;
        StringBuilder sb;
    }

    public ObjectPipe(ReadableObjectPipeInterface<InputType> previous) {
        this.next = null;
        this.previous = previous;
    }

    @Override
    public synchronized OutputType produce() {
        InputType input = previous.produce();
        OutputType output = process(input);
        return output;
    }

    public final synchronized void consume(InputType input) {
        OutputType output = process(input);
        next.consume(output);
    }

    @Override
    public synchronized final void flushPipe() {
        OutputType output = null;
        try {
            output = flushToOutput();
        } finally {
            if (output != null && next != null) {
                next.consume(output);
            }
            next.flushPipe();
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
