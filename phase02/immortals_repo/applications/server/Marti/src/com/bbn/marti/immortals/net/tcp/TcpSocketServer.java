package com.bbn.marti.immortals.net.tcp;

import com.bbn.marti.immortals.data.SocketData;
import com.bbn.marti.immortals.data.TcpInitializationData;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A Server that listens for incoming TCP connections and returns {@link Socket} when clients connect.
 *
 * @author kusbeck
 */
public class TcpSocketServer extends Transport {

    private static Logger log = Logger.getLogger(TcpSocketServer.class);


    private ConsumingPipe<SocketData> outputClientConnected;
    public final StartListeningForClients startListeningForClients;
    ServerSocket serverSocket = null;
    String name = "TcpSocketServer";

    public TcpSocketServer(@Nonnull TcpInitializationData data) throws IOException {
        super(data);
        this.name = data.name;
        startListeningForClients = new StartListeningForClients(this);

        serverSocket = new ServerSocket(data.port);
        if (port <= 0) {
            this.port = serverSocket.getLocalPort();
        }
    }

    public StartListeningForClients startListeningForClients() {
        return startListeningForClients;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void startListening() {
        log.debug("starting thread to listen on " + this);

        Thread srv = new Thread(new Runnable() {
            @Override
            public void run() {
                //TODO: make this a stop-able/restart-able thread.
                while (!serverSocket.isClosed()) {
                    try {
                        // block on accept... then, when a connection is made
                        // spawn a new thread and receive the messages
                        final Socket clientSocket = serverSocket.accept();
                        SocketData socketData = new SocketData("stcp", name, clientSocket);
                        outputClientConnected.consume(socketData);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
//                        log.error("Error spawning socket listening thread: " + e.getMessage());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
//                        log.error("Error processing new client: " + e.getMessage());
                    }
                }
                log.info("Server socket closed; shutting down accept thread");
            }
        });
        srv.setName("TCPServer:" + serverSocket.getLocalPort());
        srv.start();
    }

    @Override
    public void startListening(Socket NA) {
        startListening();
    }

    @Override
    public String toString() {
        return "Server TCP " + super.toString();
    }

    @Override
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    @Override
    protected void handleData(byte[] data) {
        // write to an accepting socket always fails
    }

    @Override
    protected void close(Transport transport) {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                remoteDisconnected.consume(null);
            } catch (IOException e) {
                System.err.println("Error closing server socket. Transport: " + transport.toString());
            }
        }
    }

    @Override
    public int getLocalPort() {
        return serverSocket.getLocalPort();
    }

    public void setOutputClientConnectedListener(ConsumingPipe<SocketData> outputClientConnected) {
        this.outputClientConnected = outputClientConnected;
    }


    public class StartListeningForClients implements ConsumingPipe<Void> {

        private final TcpSocketServer tcpSocketServer;

        public StartListeningForClients(TcpSocketServer tcpSocketServer) {
            this.tcpSocketServer = tcpSocketServer;
        }

        @Override
        public void consume(Void aVoid) {
            tcpSocketServer.startListening();
        }

        @Override
        public void flushPipe() {
        }

        @Override
        public void closePipe() {
        }
    }
}
