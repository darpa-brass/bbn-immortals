package com.bbn.marti.immortals.net.tcp;

import mil.darpa.immortals.core.*;
import com.bbn.marti.immortals.data.TcpInitializationData;
import org.jetbrains.annotations.NotNull;

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

    protected final ReceiveFromNetworkPipe receiveFromNetworkPipe = new ReceiveFromNetworkPipe();
    protected final RemoteDisconnected remoteDisconnected = new RemoteDisconnected();
    protected final SendToNetworkPipe sendToNetworkPipe;
    protected final String name;
    InetAddress host = null;
    int port = -1; // initialize to invalid

    public Transport(@NotNull TcpInitializationData initData) {
        setEndpoint(initData.address, initData.port);
        this.name = initData.name;
        this.sendToNetworkPipe = new SendToNetworkPipe(this);
    }

    protected abstract void handleData(byte[] data);

    public String getName() {
        return name;
    }

    protected abstract void close(Transport transport);

    public ReceiveFromNetworkPipe receiveFromNetworkPipe() {
        return receiveFromNetworkPipe;
    }

    public RemoteDisconnected remoteDisconnected() {
        return remoteDisconnected;
    }

    public SendToNetworkPipe sendToNetworkPipe() {
        return sendToNetworkPipe;
    }

    public void setEndpoint(@NotNull InetAddress host, @NotNull int port) {
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
        receiveFromNetworkPipe.distributeResult(Arrays.copyOf(received, bytesRead));
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

    /**
     * Produces {@link byte[]} received from the network connection
     */
    public class ReceiveFromNetworkPipe extends AbstractOutputProvider<byte[]> {

        protected void distributeResult(byte[] bytes) {
            super.distributeResult(bytes);
        }

    }

    public class RemoteDisconnected extends AbstractOutputProvider<Void> {

        protected void distributeResult(Void data) {
            super.distributeResult(data);
        }

    }

    /**
     * Consumes {@link byte[]} received from the network connection
     */
    public class SendToNetworkPipe implements InputProviderInterface<byte[]> {

        private final Transport transport;

        public SendToNetworkPipe(@NotNull Transport transport) {
            this.transport = transport;

        }

        @Override
        public void handleData(byte[] data) {
            transport.handleData(data);
        }
    }

}
