package com.bbn.immortals.archmodel;

import com.bbn.immortals.archmodel.example.ConsumerProfit;
import com.bbn.immortals.archmodel.example.ProducerFoo;
import com.bbn.immortals.archmodel.example.ConsumerProducerBar;

public class Main {

    public static void main(String[] args) {

        // Construct a foo producer to produce Foos.
        ProducerFoo foo = new ProducerFoo();

        // Bar producer/consumer, Converts Foos into Foobars, and takes AppendableStrings as input and outputs Strings
        ConsumerProducerBar bar = new ConsumerProducerBar();

        // A consumerProfit that consumes Strings. Takes Strings as input
        ConsumerProfit consumerProfit = new ConsumerProfit();

        // Add the successive chain of responsibility
        foo.addSuccessor(bar);
        bar.addSuccessor(consumerProfit);

        foo.produceFoo();
        foo.produceFoo();
        foo.produceFoo();
    }
}
