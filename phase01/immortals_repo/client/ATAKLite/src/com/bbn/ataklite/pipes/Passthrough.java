package com.bbn.ataklite.pipes;

import mil.darpa.immortals.core.synthesis.ObjectPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ReadableObjectPipeInterface;
import mil.darpa.immortals.core.synthesis.interfaces.WriteableObjectPipeInterface;

/**
 * Created by awellman@bbn.com on 6/22/16.
 */
public class Passthrough<T> extends ObjectPipe<T, T> {

    public Passthrough(ReadableObjectPipeInterface<T> previous) {
        super(previous);
    }

    public Passthrough(WriteableObjectPipeInterface<T> next) {
        super(next);
    }


    @Override
    protected T process(T t) {
        return t;
    }

    @Override
    protected T flushToOutput() {
        return null;
    }

    @Override
    protected void preNextClose() {

    }

    @Override
    public int getBufferSize() {
        return -1;
    }
}
