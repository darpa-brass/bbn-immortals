package mil.darpa.immortals.analysis.analytics;

import org.apache.log4j.*;
import org.apache.log4j.net.SocketNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
public class Log4jAnalyticsServer {

    private volatile boolean keepRunning = true;

    private Thread serverThread;

    private ServerSocket serverSocket;

    private Logger logger = Logger.getLogger(Log4jAnalyticsServer.class);

    private Set<Socket> clientSocketSet = Collections.newSetFromMap(new ConcurrentHashMap<Socket, Boolean>());

    // The name must match the logger on the client you want to receive the logs from!
    private Logger immortalsAnalyticsLogger = Logger.getLogger("ImmortalsAnalytics");

    private final int port;

    public Log4jAnalyticsServer(int port) {
//        try {
            this.port = port;
            BasicConfigurator.configure();

//            if (logFilepath == null) {
//
//                ConsoleAppender appender = new ConsoleAppender(
//                        new PatternLayout("%m%n")
//                );
//                immortalsAnalyticsLogger.addAppender(appender);
//
//            } else {
//
//                FileAppender fileAppender = new FileAppender(
//                        new PatternLayout("%m%n"),
//                        logFilepath);
//                immortalsAnalyticsLogger.addAppender(fileAppender);
//
//            }

//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }
//
//    public Log4jAnalyticsServer(int port, @Nonnull ValidatorAppender[] appenders) {
//        this.port = port;
//        BasicConfigurator.configure();
//        immortalsAnalyticsLogger.setAdditivity(false);
//
//        for (ValidatorAppender appender : appenders) {
//            immortalsAnalyticsLogger.addAppender(appender);
//        }
//    }

    public void initFileLogger(@Nonnull String logFilepath) throws IOException {
        FileAppender fileAppender = new FileAppender(
                new PatternLayout("%m%n"),
                logFilepath);
        immortalsAnalyticsLogger.addAppender(fileAppender);
    }

    public void initConsoleLogger() {
        ConsoleAppender appender = new ConsoleAppender(
                new PatternLayout("%m%n")
        );
        immortalsAnalyticsLogger.addAppender(appender);
    }

    public void addValidator(@Nonnull ValidatorAppender validatorAppender) {
        immortalsAnalyticsLogger.addAppender(validatorAppender);
    }

//    public void addValidation

    public void start() {
        logger.info("Starting log4j server on port " + port + ".");
        try {
            serverSocket = new ServerSocket(port);

            serverThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        while (keepRunning) {
                            try {
                                Socket socket = serverSocket.accept();
                                clientSocketSet.add(socket);

                                Thread clientThread = new Thread(new SocketNode(socket, LogManager.getLoggerRepository()), "ImmortalsAnalyticsClientNode");
                                clientThread.start();

                                logger.info("Connected client " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " to port " + socket.getLocalPort() + ".");

                            } catch (SocketException e) {
                                // If the server has shut down, Ignore
                                if (keepRunning) {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            logger.info("Shutting down log4j server on port " + port + ".");
            keepRunning = false;

            if (serverThread != null) {
                serverThread.interrupt();
            }

            for (Socket socket : clientSocketSet) {
                if (!socket.isClosed() && socket.isConnected()) {
                    logger.info("Disconnecting client " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + " connected to port " + socket.getLocalPort() + ".");
                    socket.close();
                }
            }

            if (serverSocket != null) {
                serverSocket.close();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}