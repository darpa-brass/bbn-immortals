package mil.darpa.immortals.core.synthesis;

import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

/**
 * This class allows for synchronized access to a {@link ObjectPipeMultiplexerTail} to ensure data is properly sent together
 * <p>
 * Created by awellman@bbn.com on 6/22/16.
 */
public class ObjectPipeMultiplexerHead<InputType0, InputType1> {

    private final ConsumingPipe<InputType0> pipe0;
    private final ConsumingPipe<InputType1> pipe1;

    public ObjectPipeMultiplexerHead(ConsumingPipe<InputType0> pipe0, ConsumingPipe<InputType1> pipe1) {
        this.pipe0 = pipe0;
        this.pipe1 = pipe1;
    }

    public synchronized void write(InputType0 object0, InputType1 object1) {
        pipe0.consume(object0);
        pipe1.consume(object1);
    }
}
