package mil.darpa.immortals.core;

/**
 * Provides an input for the data type specified
 *
 * @param <INPUT> The input data type to process
 *                <p>
 *                Created by awellman@bbn.com on 12/14/15.
 */
public interface InputProviderInterface<INPUT> {

    /**
     * Handles data
     *
     * @param data The data to be handled of type {@link INPUT}
     */
    void handleData(INPUT data);
}
