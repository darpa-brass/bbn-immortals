package com.bbn.ataklite.pipes;

import com.bbn.ataklite.CoTMessage;
import mil.darpa.immortals.core.synthesis.interfaces.ProducingPipe;

import java.io.UnsupportedEncodingException;

/**
 * Created by awellman@bbn.com on 7/18/16.
 */
public class ByteCotifier implements ProducingPipe<CoTMessage> {

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
    public synchronized CoTMessage produce() {
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
        CoTMessage cot = new CoTMessage(message);
        fragments.delete(0, endOfMessage);

        return cot;
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
