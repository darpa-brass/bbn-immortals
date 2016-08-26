package mil.darpa.immortals.core;

/**
 * Provides an output to be connected to {@link InputProviderInterface}
 *
 * @param <OUTPUT> The output data type
 *                 <p>
 *                 Created by awellman@bbn.com on 12/14/15.
 */
public interface OutputProviderInterface<OUTPUT> {

    // TODO: Handle cloning, if necessary

    /**
     * Adds a successor input to process the data produced by this module
     *
     * @param handler The handler to process the data
     */
    void addSuccessor(InputProviderInterface<OUTPUT> handler);

    /**
     * Removes a successor input to process the data produced by this module
     *
     * @param handler The handler to remove
     */
    void removeSuccessor(InputProviderInterface<OUTPUT> handler);
}
