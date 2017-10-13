package mil.darpa.immortals.analytics.protocols.log4j;

import mil.darpa.immortals.analytics.events.data.ServerClientData;
import mil.darpa.immortals.core.analytics.Analytics;
import mil.darpa.immortals.core.analytics.AnalyticsEndpointInterface;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
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
    private final Logger immortalsAnalyticsLogger = Logger.getLogger("ImmortalsAnalytics");

    private final int port;

    Log4jClientServer(int port, Appender... appenders) {
        this.port = port;

        immortalsAnalyticsLogger.setLevel(Level.INFO);
        
        for (Appender a : appenders) {
            immortalsAnalyticsLogger.addAppender(a);
        }
    }
    
    Log4jClientServer(int port, AnalyticsEndpointInterface... endpoints) {
        this.port = port;
        
        immortalsAnalyticsLogger.setLevel(Level.INFO);
        
        if (endpoints != null) {
            for( AnalyticsEndpointInterface e : endpoints) {
                immortalsAnalyticsLogger.addAppender(new Log4jAnalyticsEndpointAppender(e));
            }
        }
    }

    synchronized void start() {
        try {

            serverSocket = new ServerSocket(port);
            Analytics.log(Analytics.newEvent(AnalyticsEventType.Tooling_ValidationServerStarted, port));

            serverThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        while (isRunning) {
                            try {
                                Socket socket = serverSocket.accept();
                                clientSocketSet.add(socket);

                                Thread clientThread = new Thread(new SocketNode(socket, LogManager.getLoggerRepository()), "ImmortalsAnalyticsClientNode");
                                clientThread.start();


                                Analytics.log(Analytics.newEvent(AnalyticsEventType.Tooling_ValidationServerClientConnected,
                                        new ServerClientData(socket.getInetAddress().getHostAddress(),
                                                socket.getPort(), socket.getLocalPort())));

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

            Analytics.registerThread(serverThread);
            serverThread.start();
            isRunning = true;

        } catch (IOException e) {
            if (isRunning) {
                throw new RuntimeException(e);
            }
        }
    }

    synchronized void stop() {
        try {
            if (isRunning) {
                isRunning = false;

                if (serverThread != null) {
                    serverThread.interrupt();
                }

                for (Socket socket : clientSocketSet) {
                    if (!socket.isClosed() && socket.isConnected()) {

                        Analytics.log(Analytics.newEvent(AnalyticsEventType.Tooling_ValidationServerClientDisconnected,
                                new ServerClientData(socket.getInetAddress().getHostAddress(),
                                        socket.getPort(), socket.getLocalPort())));

                        socket.close();
                    }
                }

                if (serverSocket != null) {
                    serverSocket.close();
                    serverSocket = null;
                }

                Analytics.newEvent(AnalyticsEventType.Tooling_ValidationServerStopped, port);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
