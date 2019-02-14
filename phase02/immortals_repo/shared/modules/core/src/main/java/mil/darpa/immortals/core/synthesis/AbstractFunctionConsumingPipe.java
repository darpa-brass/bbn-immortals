package mil.darpa.immortals.core.synthesis;

import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

/**
 * Created by awellman@bbn.com on 7/25/17.
 */
public abstract class AbstractFunctionConsumingPipe<InputType, OutputType> implements ConsumingPipe<InputType> {

    private ConsumingPipe<OutputType> next;
    private final boolean distributeNull;

    public AbstractFunctionConsumingPipe(boolean distributeNull, ConsumingPipe<OutputType> next) {
        this.distributeNull = distributeNull;
        this.next = next;
    }

    public void setNext(ConsumingPipe<OutputType> next) {
        if (this.next != null) {
            throw new RuntimeException("UHOH!!");
        }
        this.next = next;
    }

    @Override
    public void consume(InputType input) {
        OutputType rval = process(input);

        if (rval != null || distributeNull) {
            next.consume(rval);
        }
    }

    public abstract OutputType process(InputType input);

    @Override
    public void flushPipe() {
        next.flushPipe();
    }

    @Override
    public void closePipe() {
        next.closePipe();
    }
}
