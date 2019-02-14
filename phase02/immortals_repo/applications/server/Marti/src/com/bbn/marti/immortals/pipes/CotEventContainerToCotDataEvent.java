package com.bbn.marti.immortals.pipes;

import com.bbn.cot.CotEventContainer;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import mil.darpa.immortals.datatypes.cot.CotHelper;
import mil.darpa.immortals.datatypes.cot.Event;

import javax.xml.bind.JAXBException;

/**
 * Created by awellman@bbn.com on 8/7/17.
 */
public class CotEventContainerToCotDataEvent implements ConsumingPipe<CotEventContainer> {
    
    private final ConsumingPipe<Event> next;
    
    public CotEventContainerToCotDataEvent(ConsumingPipe<Event> next) {
        this.next = next;
    }
    
    @Override
    public void consume(CotEventContainer cotEventContainer) {
        try {
            Event e = CotHelper.unmarshalEvent(cotEventContainer.asXml());
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
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
