package mil.darpa.immortals.analytics.protocols.log4j;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.core.analytics.AnalyticsEndpointInterface;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketAppender;

/**
 * Created by awellman@bbn.com on 7/20/16.
 */
public class Log4jAnalyticsEndpoint implements AnalyticsEndpointInterface {

    // The name must match the logger on the server to receive the logs!
    private static Logger immortalsAnalyticsLogger = Logger.getLogger("ImmortalsAnalytics");
    private static Logger logger = Logger.getLogger(Log4jAnalyticsEndpoint.class);
    private static final Gson gson = new GsonBuilder().create();
    private SocketAppender socketAppender;

    public Log4jAnalyticsEndpoint(String serverUrl, int port) {
        logger.setLevel(Level.ALL);
        immortalsAnalyticsLogger.setLevel(Level.ALL);
        BasicConfigurator.configure();

        logger.info("Attempting to connect Log4jAnalyticsEndpoint to server at '" + serverUrl + "' using port " + port + ".");

        socketAppender = new SocketAppender(serverUrl, port);
        socketAppender.setName("ImmortalsAnalyticsClientSocketAppender");
        socketAppender.setReconnectionDelay(5000);
        immortalsAnalyticsLogger.setAdditivity(false);
        immortalsAnalyticsLogger.addAppender(socketAppender);
    }

    public void log(AnalyticsEvent e) {
        String str = gson.toJson(e);
        logger.trace("Logging '" + str + "'.");
        immortalsAnalyticsLogger.info(str);
    }
    
    @Override
    public void start() {
        // pass
    }

    @Override
    public void shutdown() {
    
        if (socketAppender != null) {
            socketAppender.close();
        }
    }
}
