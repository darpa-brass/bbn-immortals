package com.bbn.marti.immortals.cot;

import com.bbn.cot.CotEventContainer;
import com.bbn.cot.CotParser;
import com.bbn.marti.immortals.net.tcp.Transport;
import com.bbn.marti.remote.BadgeOfShame;
import com.bbn.marti.service.CoreMonitor;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A {@link Map} of {@link Transport} to {@link StringBuilder},
 * such that when a new CoT string is received and added to this processor,
 * this will return the {@link CotEventContainer} when the complete CoT event is received.
 *
 * @author kusbeck
 */
public class StreamingCotProcessor {

    private static final int INDIVIDUAL_COT_MSG_SIZE_LIMIT = 8388608; // 8MB
    private static final String END_OF_COT_MSG_STR = "</event>";
    private static final int BUFFER_RESET_SIZE = 1000000; // 1MB
    private static Logger log = Logger.getLogger(StreamingCotProcessor.class);

    protected Map<StreamingCotProtocol, StringBuilder> builderMap = new HashMap<>();
    //    protected Map<Transport, StringBuilder> builderMap = new HashMap<Transport, StringBuilder>();
    protected LinkedList<BadgeOfShame> badMessages;

    public StreamingCotProcessor() {
        badMessages = (LinkedList<BadgeOfShame>) CoreMonitor.getInstance().getAttribute(BadgeOfShame.SHAMEWALL_KEY);
    }

    private StringBuilder getOrInit(StreamingCotProtocol toFind) {
        synchronized (builderMap) {
            if (!builderMap.containsKey(toFind)) {
                builderMap.put(toFind, new StringBuilder());
            }
            return builderMap.get(toFind);
        }
    }

    /**
     * appends the incoming string s from the given transport to the current buffer for that transport.
     *
     * @param s
     * @param streamingCotProtocol
     * @return
     * @author simon chase
     * If a start and end tag are found (earliest start tag, earliest end tag), and the start tag occurs before the end tag,
     * then the contained content is removed from the buffer, and we attempt to parse it into a Document (inside a CotEventContainer).
     * Otherwise,
     * - for as long as there is a start tag without an end tag, the buffer is grown until it exceeds the size limit, and then discarded.
     */
    public List<CotEventContainer> add(String s, StreamingCotProtocol streamingCotProtocol) {
        List<CotEventContainer> result = new LinkedList<CotEventContainer>();
        StringBuilder strBuilder = getOrInit(streamingCotProtocol);

        // init search pointer (0 for all other iterations, but we start searching after the end of the message)
        // note: subtract the end-tag length to cover end tag spanning multiple frames
        int prevLen = Math.max(0, strBuilder.length() - END_OF_COT_MSG_STR.length());

        strBuilder.append(s);
        int indexOfEnd = -1;

        while ((indexOfEnd = strBuilder.indexOf(END_OF_COT_MSG_STR, prevLen)) >= 0) {
            // have some end tag to seek to
            int closeIndex = indexOfEnd + END_OF_COT_MSG_STR.length();
            final String msg = strBuilder.substring(0, closeIndex);

            if (msg.length() <= INDIVIDUAL_COT_MSG_SIZE_LIMIT) {
                // have a message window to parse, within size limits
                try {
                    CotEventContainer tmp = new CotEventContainer(CotParser.parse(msg));
                    //log.warn("Successfully bunched " + tmp.getDocument().asXML());
                    result.add(tmp);
                } catch (Exception e) {
                    // TODO: handle potential nesting issues here
                    log.warn("Error parsing CoT message:\n" + msg + " : " + e.getMessage());
                    badMessages.add(new BadgeOfShame(msg, streamingCotProtocol.toString(), e));
                    if (badMessages.size() > 50) {
                        badMessages.pop();
                    }
                }
                // delete up to close tag of what we tried to (maybe successfully) parsed
            } else {
                // message closure is too long, even if we could parse it -- chuck out
                log.warn("Error parsing CoT message: message too long to parse: " + strBuilder.substring(0, closeIndex));
            }
            strBuilder.delete(0, closeIndex);
            // END OF WHILE -- have cleared or parsed past the end tag that we found
            prevLen = 0; // reset search finger
        }

		/* 
            check remainder left in string builder for overflow -- know there is no end tag left
		*/
        if (strBuilder.length() > INDIVIDUAL_COT_MSG_SIZE_LIMIT) {
            log.warn("deleting oversize message: " + strBuilder.substring(0, strBuilder.length()));
            strBuilder.delete(0, strBuilder.length());
        }

        if (strBuilder.capacity() > BUFFER_RESET_SIZE) {
            // trim buffer size down if we've grown beyond a meg.
            log.warn("trimming buffer down to size");
            strBuilder.trimToSize();
        }

		/* POST: 
            - No close tag is in the buffer
			- If there is a start tag:
				- it is aligned with the start of the string builder
				- the length of the buffer beyond after it does not exceed the message size bound
			- If there is no start tag, then the buffer is empty
		*/
        return result;
    }

    /**
     * Remove a transport from the list of transport that is being processed by this processor.
     *
     * @param transport
     */
    public void removeBuilder(Transport transport) {
        synchronized (builderMap) {
            StringBuilder strBuilder = builderMap.get(transport);
            if (strBuilder != null) {
                if (strBuilder.toString().trim().length() > 0) {
                    badMessages.add(new BadgeOfShame(strBuilder.toString(), transport.toString(),
                            new Exception("Incomplete Message")));
                }
                builderMap.remove(transport);
            }
        }
    }
}
