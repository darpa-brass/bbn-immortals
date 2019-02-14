package com.bbn.marti.immortals.pipes;

import com.bbn.cot.CotEventContainer;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

/**
 * This DFU extracts the CoT message as bytes from a CotEventContainer
 * Created by awellman@bbn.com on 1/11/16.
 */
public class CotEventContainerBytesExtractionPipe implements ConsumingPipe<CotEventContainer> {
    
    private final ConsumingPipe<byte[]> next;
    
    public CotEventContainerBytesExtractionPipe(ConsumingPipe<byte[]> next) {
        this.next = next;
    }
    
    
    @Override
    public void consume(CotEventContainer data) {
        next.consume(data.asXml().getBytes());
    }

    @Override
    public void flushPipe() {
        next.flushPipe();
    }

    @Override
    public void closePipe() {
        next.closePipe();

    }
}
