package mil.darpa.immortals.analytics.protocols.log4j;

import mil.darpa.immortals.core.analytics.AnalyticsEndpointInterface;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketAppender;

/**
 * Created by awellman@bbn.com on 7/20/16.
 */
public class Log4jAnalyticsEndpoint implements AnalyticsEndpointInterface {

    // The name must match the logger on the server to receive the logs!
    private static Logger immortalsAnalyticsLogger = Logger.getLogger("ImmortalsAnalytics");
    private static Logger logger = Logger.getLogger(Log4jAnalyticsEndpoint.class);
    private SocketAppender socketAppender;

    public Log4jAnalyticsEndpoint(String serverUrl, int port) {
        BasicConfigurator.configure();

        logger.info("Attempting to connect Log4jAnalyticsEndpoint to server at '" + serverUrl + "' using port " + port + ".");

        socketAppender = new SocketAppender(serverUrl, port);
        socketAppender.setName("ImmortalsAnalyticsClientSocketAppender");
        socketAppender.setReconnectionDelay(5000);
        immortalsAnalyticsLogger.setAdditivity(false);
        immortalsAnalyticsLogger.addAppender(socketAppender);
    }

    public void log(String s) {
        logger.trace("Logging '" + s + "'.");
        immortalsAnalyticsLogger.info(s);
    }

    @Override
    public void shutdown() {
        if (socketAppender != null) {
            socketAppender.close();
        }
    }
}
