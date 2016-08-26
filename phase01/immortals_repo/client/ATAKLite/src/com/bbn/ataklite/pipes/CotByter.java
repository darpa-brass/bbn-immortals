package com.bbn.ataklite.pipes;

import com.bbn.ataklite.CoTMessage;
import mil.darpa.immortals.core.synthesis.ObjectPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ReadableObjectPipeInterface;
import mil.darpa.immortals.core.synthesis.interfaces.WriteableObjectPipeInterface;

/**
 * Converts Cot to Bytes
 */
public class CotByter extends ObjectPipe<CoTMessage, byte[]> {

    public CotByter(ReadableObjectPipeInterface<CoTMessage> previous) {
        super(previous);
    }

    public CotByter(WriteableObjectPipeInterface<byte[]> next) {
        super(next);
    }

    @Override
    protected byte[] process(CoTMessage coTMessage) {
        //This handler is responsible for conversion from a CotMessage to a ByteBuffer, but it delegates
        //these functions to the CotMessage and String classes
        return coTMessage.getAsXML().getBytes();
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
