package mil.darpa.immortals.core.synthesis.interfaces;

import java.io.IOException;

/**
 * A simple interface to define a consume method for elements of a pipeline
 * Created by awellman@bbn.com on 6/22/16.
 */
public interface WriteableObjectPipeInterface<InputType> {
    public void consume(InputType input);
    public void flushPipe();
    public void closePipe();
}
