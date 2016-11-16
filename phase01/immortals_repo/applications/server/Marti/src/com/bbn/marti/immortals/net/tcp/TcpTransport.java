package com.bbn.marti.immortals.net.tcp;

import com.bbn.marti.immortals.data.TcpInitializationData;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A {@link Transport} that sends data over the reliable, in-order TCP {@link Socket}.
 *
 * @author kusbeck
 */
public class TcpTransport extends Transport {

    static ExecutorService threadPool = Executors.newCachedThreadPool();
    private static Logger log = Logger.getLogger(TcpTransport.class);
    public Socket clientSocket = null;
    private Socket tmpHolder = null;

    public TcpTransport(@NotNull TcpInitializationData data) {
        super(data);
    }

    /**
     * Connects to host:port - if a socket doesn't already exist, it will create one.
     */
    @Override
    synchronized protected void connect() throws IOException {
        if (clientSocket == null || clientSocket.isClosed()) {  // also check if bound? mjg
            connect(new Socket(host, port)); // informs listeners of onConnect()
        }
    }

    /**
     * Uses an already-existing {@link Socket} to connect
     *
     * @param socket
     * @return
     * @throws IOException
     */
    protected void connect(Socket socket) throws IOException {
        super.setEndpoint(socket.getInetAddress(), socket.getPort());
        clientSocket = socket;
        // socket is set, now inform listeners of onConnect()
        super.connect();
    }

    @Override
    protected void close(Transport transport) {
        try {
            if (clientSocket != null) {
                // close the socket
                clientSocket.close();

                // let go of pointer to the socket
                clientSocket = null;

                // Inform the listeners
                remoteDisconnected.distributeResult(null);
            }
        } catch (IOException e) {
            log.error("Error closing TcpTransport " + this + ": " + e.getMessage());
        }
    }

    @Override
    protected void handleData(byte[] data) {
        try {
            if (clientSocket == null || !clientSocket.isConnected()) {
                //TODO don't do this until we fix the removal of old subscriptions
                //connect();
                // do nothing in the meantime
                return;
            }

            DataOutputStream writer = new DataOutputStream(clientSocket.getOutputStream());
            writer.write(data);
            //log.debug("wrote msg to " + this + ": " + toWrite);
        } catch (IOException e) {
            System.err.println("Error sendind data to " + this.toString());
            clientSocket = null; // force a reconnect
        }
    }

    public String toString() {
        if (this.getHost() != null)
            return "tcp:" + this.getHost().getHostAddress() + ":" + this.getPort();
        return "TCP " + super.toString();
    }

    /**
     * Connects and starts a thread listening on this socket
     */
    @Override
    public void startListening() throws IOException {
        threadPool.execute(new SocketHandlerThread(this));
    }

    @Override
    public void startListening(Socket s) throws IOException {
        tmpHolder = s;
        startListening();
    }

    @Override
    public int getLocalPort() {
        return clientSocket.getLocalPort();
    }

    /**
     * Thread for listening to data from a socket.
     *
     * @author kusbeck
     */
    class SocketHandlerThread implements Runnable {
        public static final int BUFFER_SIZE = 700;
        TcpTransport transport;

        public SocketHandlerThread(TcpTransport transport) {
            this.transport = transport;
            //setName("TCPRcvTransport:"+transport.host.getHostAddress()+":"+transport.getPort());
        }

        @Override
        public void run() {
            try {
                if (tmpHolder == null) {
                    connect();
                } else {
                    connect(tmpHolder);
                    tmpHolder = null;
                }
                InputStream reader = transport.clientSocket.getInputStream();
                int bytesRead = 0;
                byte[] bytes = new byte[BUFFER_SIZE];
                while ((bytesRead = reader.read(bytes)) >= 0) {
                    if (bytesRead > 0) {
                        byte[] receivedData = Arrays.copyOf(bytes, bytesRead);
                        receiveFromNetworkPipe.distributeResult(receivedData);
                    } else {
                        log.warn("Read zero bytes on socket: " + transport);
                    }
                }

            } catch (IOException e) {
                log.error("Error reading from socket: " + e.getMessage());
            }

            // Once the stream hits EOF, clean-up after ourselves...
            // Note: This fixes the "SocketException: Too many open files" error.
            transport.close(transport);
        }
    }
}
