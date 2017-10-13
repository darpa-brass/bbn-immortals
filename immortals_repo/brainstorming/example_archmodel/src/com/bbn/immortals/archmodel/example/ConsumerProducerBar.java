package com.bbn.immortals.archmodel.example;

import com.bbn.immortals.archmodel.InputOutputInterface;
import com.bbn.immortals.archmodel.InputProviderInterface;

import java.util.LinkedList;
import java.util.List;

/**
 * This takes in an {@link AppendableString} and adds "Bar" to it
 *
 * Created by awellman@bbn.com on 12/11/15.
 */
public class ConsumerProducerBar implements InputOutputInterface<AppendableString, String> {

    private static final List<InputProviderInterface> handlerList = new LinkedList<>();

    public ConsumerProducerBar() {

    }

    @Override
    public synchronized void handleData(AppendableString data) {
        AppendableString oldData = data;
        AppendableString newData = new AppendableString(oldData.getValue() + "Bar");

        for (InputProviderInterface handler : handlerList) {
            handler.handleData(newData.getValue());
        }
    }

    @Override
    public synchronized void addSuccessor(InputProviderInterface<String> handler) {
        handlerList.add(handler);
    }

    @Override
    public synchronized void removeSuccessor(InputProviderInterface<String> handler) {
        handlerList.remove(handler);
    }
}
