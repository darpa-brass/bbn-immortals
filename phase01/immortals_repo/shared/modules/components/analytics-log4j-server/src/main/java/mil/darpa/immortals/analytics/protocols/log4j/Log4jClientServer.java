package mil.darpa.immortals.analytics.protocols.log4j;

import mil.darpa.immortals.analytics.L;
import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketNode;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by awellman@bbn.com on 7/20/16.
 */
class Log4jClientServer {

    private volatile boolean isRunning = false;

    private Thread serverThread;

    private ServerSocket serverSocket;

    private Set<Socket> clientSocketSet = Collections.newSetFromMap(new ConcurrentHashMap<Socket, Boolean>());

    // The name must match the logger on the client you want to receive the logs from!
    private Logger immortalsAnalyticsLogger = Logger.getLogger("ImmortalsAnalytics");

    private final int port;

    public Log4jClientServer(int port, Appender appender) {
        this.port = port;
        immortalsAnalyticsLogger.addAppender(appender);
    }

    public synchronized void start() {
        try {
            serverSocket = new ServerSocket(port);
            L.validationServerStarted(port);

            serverThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        while (isRunning) {
                            try {
                                Socket socket = serverSocket.accept();
                                clientSocketSet.add(socket);

                                Thread clientThread = new Thread(new SocketNode(socket, LogManager.getLoggerRepository()), "ImmortalsAnalyticsClientNode");
                                clientThread.start();

                                L.clientConnected(socket.getInetAddress().getHostAddress(), socket.getPort(), socket.getLocalPort());

                            } catch (SocketException e) {
                                // If the server has shut down, Ignore
                                if (isRunning) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            serverThread.start();
            isRunning = true;

        } catch (IOException e) {
            if (isRunning) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void stop() {
        try {
            if (isRunning) {
                isRunning = false;

                if (serverThread != null) {
                    serverThread.interrupt();
                }

                for (Socket socket : clientSocketSet) {
                    if (!socket.isClosed() && socket.isConnected()) {
                        L.clientDisconnected(socket.getInetAddress().getHostAddress(), socket.getPort(), socket.getLocalPort());
                        socket.close();
                    }
                }

                if (serverSocket != null) {
                    serverSocket.close();
                    serverSocket = null;
                }

                L.validationServerStopped(port);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
