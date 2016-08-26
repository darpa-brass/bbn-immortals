package com.bbn.immortals.archmodel.example;

import com.bbn.immortals.archmodel.InputProviderInterface;

/**
 * This is a consumer that simply prints a String to the command line.
 *
 * Created by awellman@bbn.com on 12/11/15.
 */
public class ConsumerProfit implements InputProviderInterface<String> {

    @Override
    public void handleData(String data) {
        System.out.println(data);
    }

}
