package com.bbn.immortals.archmodel.example;

import com.bbn.immortals.archmodel.InputProviderInterface;
import com.bbn.immortals.archmodel.OutputProviderInterface;

import java.util.LinkedList;
import java.util.List;

/**
 * This produces numbered Foo on command
 *
 * Created by awellman@bbn.com on 12/11/15.
 */
public class ProducerFoo implements OutputProviderInterface<AppendableString> {

    private final List<InputProviderInterface> handlerList = new LinkedList<>();

    private int counter = 0;

    public ProducerFoo() {
    }

    /**
     * Produce foo...
     */
    public void produceFoo() {
        AppendableString data = new AppendableString("ProducerFoo" + Integer.toString(counter));
        counter++;
        for (InputProviderInterface handler : handlerList) {
            handler.handleData(data);
        }
    }

    @Override
    public void addSuccessor(InputProviderInterface<AppendableString> handler) {
        handlerList.add(handler);
    }

    @Override
    public void removeSuccessor(InputProviderInterface<AppendableString> handler) {
        if (handlerList.contains(handler)) {
            handlerList.remove(handler);
        } else {
            System.err.println("Cannot remove handler " + handler.toString() + " because ProducerFoo does not contain it as a successor!");
        }
    }
}
