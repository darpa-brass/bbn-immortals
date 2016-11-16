package mil.darpa.immortals.analytics;

import com.google.gson.Gson;
import mil.darpa.immortals.analytics.events.data.ServerClientData;
import mil.darpa.immortals.analytics.validators.result.ValidationResults;
import mil.darpa.immortals.analytics.validators.result.ValidationResultsListener;
import mil.darpa.immortals.core.analytics.Analytics;
import mil.darpa.immortals.core.analytics.AnalyticsEndpointInterface;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * Created by awellman@bbn.com on 10/25/16.
 */
public class L {


    private static Logger logger;

    private static Level environmentLogLevel = Level.DEBUG;

    private static Level subjectLogLevel = Level.DEBUG;

    private static Level resultsLogLevel = Level.INFO;

    private static final Gson gson = new Gson();

    public static void initialize(Logger logger) {
        L.logger = logger;
    }

    public static AnalyticsEndpointInterface getAnalyticsEndpointInstance() {
        return new AnalyticsEndpointInterface() {

            @Override
            public void log(String eventPayload) {
                L.log(eventPayload, subjectLogLevel);
            }

            @Override
            public void shutdown() {

            }
        };

    }

    private static <T> void log(T object, Level logLevel) {
        String data;

        if (object instanceof String) {
            data = (String) object;
        } else {
            data = gson.toJson(object, object.getClass());
        }

        switch (logLevel) {
            case DEBUG:
                logger.debug(data);
                break;

            case ERROR:
                logger.error(data);
                break;

            case INFO:
                logger.info(data);
                break;

            case TRACE:
                logger.trace(data);
                break;

            case WARN:
                logger.warn(data);
                break;
        }
    }

    public static void validationFinished(ValidationResults results) {
        log(Analytics.newEvent(AnalyticsEventType.Tooling_ValidationFinished, null, results), resultsLogLevel);
    }

    public static void clientConnected(String hostAddress, int remotePort, int localPort) {
        log(Analytics.newEvent(AnalyticsEventType.Tooling_ValidationServerClientConnected, null,
                new ServerClientData(hostAddress, remotePort, localPort)), environmentLogLevel);
    }

    public static void clientDisconnected(String hostAddress, int remotePort, int localPort) {
        log(Analytics.newEvent(AnalyticsEventType.Tooling_ValidationServerClientDisconnected, null,
                new ServerClientData(hostAddress, remotePort, localPort)), environmentLogLevel);
    }

    public static void analyticsEvent(AnalyticsEvent event) {
        log(event, subjectLogLevel);
    }

    public static void validationServerStarted(int port) {
        log(Analytics.newEvent(AnalyticsEventType.Tooling_ValidationServerStarted, null, port), environmentLogLevel);
    }

    public static void validationServerStopped(int port) {
        log(Analytics.newEvent(AnalyticsEventType.Tooling_ValidationServerStopped, null, port), environmentLogLevel);
    }

}
