package com.bbn.immortals.archmodel;

/**
 * Provides an output to be connected to {@link InputProviderInterface}
 *
 * @param <OUTPUT> The output data type
 *
 * Created by awellman@bbn.com on 12/14/15.
 */
public interface OutputProviderInterface<OUTPUT> {

    // TODO: Handle cloning, if necessary

    /**
     * Adds a successor input to process the data produced by this module
     * @param handler The handler to process the data
     */
    public void addSuccessor(InputProviderInterface<OUTPUT> handler);

    /**
     * Removes a successor input to process the data produced by this module
     * @param handler The handler to remove
     */
    public void removeSuccessor(InputProviderInterface<OUTPUT> handler);
}
