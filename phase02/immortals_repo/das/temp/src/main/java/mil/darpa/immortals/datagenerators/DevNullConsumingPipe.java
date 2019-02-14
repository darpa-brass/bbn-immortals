package mil.darpa.immortals.datagenerators;

import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

/**
 * To the nothingness it goes....
 * <p>
 * Created by awellman@bbn.com on 10/7/16.
 */
public class DevNullConsumingPipe<InputType> implements ConsumingPipe<InputType> {

    public DevNullConsumingPipe() {

    }

    @Override
    public void consume(InputType input) {

    }

    @Override
    public void flushPipe() {

    }

    @Override
    public void closePipe() {

    }
}
