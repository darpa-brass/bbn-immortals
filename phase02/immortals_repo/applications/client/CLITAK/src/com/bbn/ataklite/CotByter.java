package com.bbn.ataklite;

import mil.darpa.immortals.core.synthesis.ObjectPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ProducingPipe;
import mil.darpa.immortals.datatypes.cot.CotHelper;
import mil.darpa.immortals.datatypes.cot.Event;

import javax.xml.bind.JAXBException;

/**
 * Converts Cot to Bytes
 */
public class CotByter extends ObjectPipe<Event, byte[]> {

    public CotByter(ProducingPipe<Event> previous) {
        super(previous);
    }

    public CotByter(ConsumingPipe<byte[]> next) {
        super(next);
    }

    @Override
    protected byte[] process(Event coTMessage) {
        //This handler is responsible for conversion from a CotMessage to a ByteBuffer, but it delegates
        //these functions to the CotMessage and String classes
        try {
            return CotHelper.marshalObject(coTMessage).getBytes();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected byte[] flushToOutput() {
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
