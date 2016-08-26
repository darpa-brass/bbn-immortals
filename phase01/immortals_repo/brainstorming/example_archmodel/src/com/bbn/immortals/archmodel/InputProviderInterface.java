package com.bbn.immortals.archmodel;

/**
 * Provides an input for the data type specified
 *
 * @param <INPUT> The input data type to process
 *
 * Created by awellman@bbn.com on 12/14/15.
 */
public interface InputProviderInterface<INPUT> {

    /**
     * Handles data
     * @param data The data to be handled of type {@link INPUT}
     */
    public void handleData(INPUT data);
}
