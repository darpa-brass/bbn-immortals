package com.bbn.marti.immortals.pipes;

import com.bbn.cot.CotEventContainer;
import com.bbn.marti.immortals.net.tcp.Transport;
import mil.darpa.immortals.core.synthesis.AbstractMultisuccessorConsumingPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import mil.darpa.immortals.datatypes.cot.dom4j.StreamingCotProcessor;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

/**
 * Cursor-on-Target (CoT) protocol adapter that re-uses a socket instead of opening and closing it between each message.
 *
 * @author awellman
 */
public class CotByteBufferPipe extends AbstractMultisuccessorConsumingPipe<byte[], CotEventContainer> {

    public final InputTransportClosed inputTransportClosed;
    private StreamingCotProcessor cotProc = new StreamingCotProcessor();

    public CotByteBufferPipe(@Nullable ConsumingPipe<CotEventContainer>... next) {
        super(false, next);
        inputTransportClosed = new InputTransportClosed(this);
    }

    /**
     * Callback that is called when data is received by the transport.
     */
    @Override
    public CotEventContainer process(byte[] input) {
        List<Document> docList = cotProc.add(new String(input, 0, input.length), this);
        List<CotEventContainer> cotList = new LinkedList<>();
        for (Document d : docList) {
            try {
                cotList.add(new CotEventContainer(d));
            } catch (DocumentException e) {
                throw new RuntimeException(e);
            }
        }
        for (CotEventContainer cot : cotList) {
            distributeOutput(cot);
        }
        return null;
    }

    /**
     * Callback when the transport is closed.
     */
    // TODO: Austin: Hook up
    protected void onClose(Transport transport) {
        cotProc.removeBuilder(transport);
    }

    public String toString() {
        return "CoT " + super.toString();
    }


    public class InputTransportClosed implements ConsumingPipe<Transport> {
        private final CotByteBufferPipe protocol;

        InputTransportClosed(CotByteBufferPipe protocol) {
            this.protocol = protocol;
        }

        @Override
        public void consume(Transport transport) {
            protocol.onClose(transport);
            cotProc.removeBuilder(transport);
        }

        @Override
        public void flushPipe() {

        }

        @Override
        public void closePipe() {

        }
    }
}
