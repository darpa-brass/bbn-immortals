package mil.darpa.immortals.core.synthesis;

import mil.darpa.immortals.core.synthesis.interfaces.ProducingPipe;

/**
 * Allows simple function-based implementation of a single part of a pipeline
 * Created by awellman@bbn.com on 6/21/16.
 */
@Deprecated
public abstract class ReadableObjectPipe<InputType, OutputType> implements ProducingPipe<OutputType> {
    private final ProducingPipe<InputType> previous;

    public ReadableObjectPipe(ProducingPipe<InputType> previous) {
        this.previous = previous;
    }

    @Override
    public OutputType produce() {
        InputType inputData = previous.produce();

        if (inputData != null) {
            return process(inputData);
        }
        return null;
    }

    @Override
    public final void closePipe() {
        previous.closePipe();
        postPreviousClose();
    }

    protected abstract OutputType process(InputType input);

    protected abstract void postPreviousClose();
}
