package com.bbn.marti.immortals.net.tcp;

import com.bbn.marti.immortals.data.TcpInitializationData;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * The transport-layer mechanism by which data will be transfered.
 *
 * @author kusbeck
 * @see TcpTransport
 * // * @see UdpTransport
 */
public abstract class Transport implements TransportInterface {

    protected ConsumingPipe<byte[]> receiveFromNetworkPipe;
    protected ConsumingPipe<Void> remoteDisconnected;
    protected final ConsumingPipe<byte[]> sendToNetworkPipe;
    protected final String name;
    InetAddress host = null;
    int port = -1; // initialize to invalid

    public Transport(@Nonnull TcpInitializationData initData) {
        setEndpoint(initData.address, initData.port);
        this.name = initData.name;
        this.sendToNetworkPipe = new SendToNetworkPipe(this);
    }

    protected abstract void handleData(byte[] data);

    public String getName() {
        return name;
    }

    protected abstract void close(Transport transport);

    public ConsumingPipe<byte[]> receiveFromNetworkPipe() {
        return receiveFromNetworkPipe;
    }

    public ConsumingPipe<byte[]> sendToNetworkPipe() {
        return sendToNetworkPipe;
    }

    public void setEndpoint(@Nonnull InetAddress host, @Nonnull int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public abstract int getLocalPort();

    /**
     * Checks host/port, calls implementation-specific {@link Transport#connect()},
     * then informs listeners that the transport is connected.
     *
     * @return the connected transport
     * @throws IOException
     * @note Don't overload this unless you also call super.connect()
     */
    protected void connect() throws IOException {
        if (host == null || port < 0) {
            throw new IOException("must set endpoint prior to connection.");
        }
    }

    @Override
    public abstract void startListening() throws IOException;

    protected void dataReceived(byte[] received, int bytesRead) {
        receiveFromNetworkPipe.consume(Arrays.copyOf(received, bytesRead));
    }

    @Override
    public InetAddress getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Transport " + ((getHost() == null) ? "" : getHost().getHostAddress())
                + ":" + getPort();
    }

    public Transport setReceiveFromNetworkPipeListener(ConsumingPipe<byte[]> next) {
        this.receiveFromNetworkPipe = next;
        return this;
    }

    public Transport setRemoteDisconnectedListener(ConsumingPipe<Void> next) {
        this.remoteDisconnected = next;
        return this;
    }

    /**
     * Consumes {@link byte[]} received from the network connection
     */
    public class SendToNetworkPipe implements ConsumingPipe<byte[]> {

        private final Transport transport;

        public SendToNetworkPipe(@Nonnull Transport transport) {
            this.transport = transport;

        }
        
        @Override
        public void consume(byte[] bytes) {
            transport.handleData(bytes);
        }

        @Override
        public void flushPipe() {

        }

        @Override
        public void closePipe() {

        }
    }

}
