package com.bbn.ataklite;

import mil.darpa.immortals.core.synthesis.interfaces.ProducingPipe;
import mil.darpa.immortals.datatypes.cot.CotHelper;
import mil.darpa.immortals.datatypes.cot.Event;

import javax.xml.bind.JAXBException;
import java.io.UnsupportedEncodingException;

/**
 * Created by awellman@bbn.com on 7/18/16.
 */
public class ByteCotifier implements ProducingPipe<Event> {

    private static final int INDIVIDUAL_COT_MSG_SIZE_LIMIT = 8388608; // 8MB

    private final byte[] workingBuffer = new byte[INDIVIDUAL_COT_MSG_SIZE_LIMIT];

    private StringBuilder fragments = new StringBuilder();
    private static final String startCotMarker = "<event";
    private static final String endCotMarker = "</event>";
    private static final int endCotMarkerLength = endCotMarker.length();

    private ProducingPipe<byte[]> previous;

    public ByteCotifier(ProducingPipe<byte[]> previous) {
        this.previous = previous;
    }

    @Override
    public synchronized Event produce() {
        int startMarker = -1;
        int endMarker = -1;
        // Until a full cot message has been received, keep getting more bytes from the source
        do {
            startMarker = fragments.indexOf(startCotMarker);
            endMarker = fragments.indexOf(endCotMarker);

            if (startMarker < 0 || endMarker < 0) {
                try {
                    byte bytes[] = previous.produce();
                    String decodedString = new String(bytes, "UTF-8");
                    fragments.append(decodedString);

                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        } while (startMarker < 0 || endMarker < 0);

        // Once there is enough, extract the String and turn it into a cot message
        int endOfMessage = endMarker + endCotMarkerLength;
        String message = fragments.substring(startMarker, endOfMessage);
        try {
            Event cot = CotHelper.unmarshalEvent(message);
            fragments.delete(0, endOfMessage);

            return cot;
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void closePipe() {
        previous.closePipe();
    }

    @Override
    public int getBufferSize() {
        return INDIVIDUAL_COT_MSG_SIZE_LIMIT;
    }
}
