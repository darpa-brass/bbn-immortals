package com.bbn.marti.immortals.cot;

import com.bbn.cot.CotEventContainer;
import mil.darpa.immortals.core.AbstractOutputProvider;
import mil.darpa.immortals.core.InputProviderInterface;
import com.bbn.marti.immortals.net.tcp.Transport;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Cursor-on-Target (CoT) protocol that re-uses a socket instead of opening and closing it between each message.
 *
 * @author kusbeck
 */
public class StreamingCotProtocol {

    private static Logger log = Logger.getLogger(StreamingCotProtocol.class);
    public final InputTransportClosed inputTransportClosed;
    private final CotEventContainerProduced cotEventContainerProduced;
    private StreamingCotProcessor cotProc = new StreamingCotProcessor();

    public StreamingCotProtocol() {
        cotEventContainerProduced = new CotEventContainerProduced();
        inputTransportClosed = new InputTransportClosed(this);
    }

    public CotEventContainerProduced cotEventContainerProduced() {
        return cotEventContainerProduced;
    }

    /**
     * Callback that is called when data is received by the transport.
     */
    public void onDataReceived(byte[] received, int bytesRead, StreamingCotProtocol streamingCotProtocol) {
        //log.debug("Received: " + received + " from " + transport);
        // this.transport = transport;
        List<CotEventContainer> cotList = cotProc.add(new String(received, 0, bytesRead), streamingCotProtocol);
        for (CotEventContainer cot : cotList) {
            cotEventContainerProduced.distributeResult(cot);
        }
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

    public class InputTransportClosed implements InputProviderInterface<Transport> {

        private final StreamingCotProtocol protocol;

        public InputTransportClosed(StreamingCotProtocol protocol) {
            this.protocol = protocol;
        }

        @Override
        public void handleData(Transport data) {
            protocol.onClose(data);
            cotProc.removeBuilder(data);
        }
    }

    public class CotEventContainerProduced extends AbstractOutputProvider<CotEventContainer> {

        protected void distributeResult(CotEventContainer data) {
            super.distributeResult(data);
        }

    }

}
