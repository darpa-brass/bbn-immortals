package mil.darpa.immortals.core.synthesis.interfaces;

/**
 * A simple interface to define a consume method for elements of a pipeline
 * Created by awellman@bbn.com on 6/22/16.
 */
public interface ConsumingPipe<InputType> {

    /**
     * The parameter list of the constructor of a ConsumingPipe can start with DFU-specific parameters, if applicable.
     * The parameter list of the constructor of a ConsumingPipe can end with the next ConsumingPipe, if applicable, but
     *      it must always be at the end following DFU-specific parameters.
     */

    /**
     * The consume function must process the input and then pass the result on to the next ConsumingPipe, if one exists.
     *
     * @param input
     */
    public void consume(InputType input);

    /**
     * This must flush the DFU behavior, if necessary.
     * If there is a successor, that pipe must be flushed as the last action in this method.
     */
    public void flushPipe();

    /**
     * This must close the DFU behavior, if necessary.
     * If there is a successor, that pipe must be closed as the last action in this method.
     */
    public void closePipe();
}
