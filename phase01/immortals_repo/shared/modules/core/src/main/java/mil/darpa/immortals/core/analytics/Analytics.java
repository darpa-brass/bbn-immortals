package mil.darpa.immortals.core.analytics;

import com.google.gson.Gson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 3/22/16.
 */
public class Analytics {

    protected static final Gson gson = new Gson();
    private static AnalyticsEndpointInterface endpoint;
    static AnalyticsVerbosity logVerbosity = AnalyticsVerbosity.Metadata;
    private static String sourceIdentifier = "UNDEFINED";

    private static LinkedList<AnalyticsEvent> logQueue;

    // TODO: Use this re reuse events instead of unnecessarily create new objects
    public static AnalyticsEvent newEvent(@Nonnull AnalyticsEventType type, @Nonnull String remoteEventSource, @Nullable Object data) {
        return new AnalyticsEvent(type, sourceIdentifier, remoteEventSource, data);
    }

    public synchronized static void initializeEndpoint(AnalyticsEndpointInterface endpointInterface) {
        endpoint = endpointInterface;
        if (logQueue != null && !logQueue.isEmpty()) {
            for (AnalyticsEvent e : logQueue) {
                endpoint.log(gson.toJson(e));
            }
        }
        if (logQueue != null) {
            logQueue.clear();
        }
    }

    public synchronized static void shutdown() {
        if (endpoint != null) {
            endpoint.shutdown();
        }
    }

    /**
     * Log an event. I am using an object to keep things flexible in case other things need to be added for analysis.
     *
     * @param analyticsEvent The event to log
     */
    public synchronized static void log(@Nonnull AnalyticsEvent analyticsEvent) {
        if (endpoint == null) {
            if (logQueue == null) {
                logQueue = new LinkedList();
            }
            logQueue.add(analyticsEvent);

        } else {
            endpoint.log(gson.toJson(analyticsEvent));
        }
    }

    public static void setSourceIdentifier(@Nonnull String identifier) {
        sourceIdentifier = identifier;
    }

    public static String getOwnSourceIdentifier() {
        return sourceIdentifier;
    }

    public static AnalyticsVerbosity getVerbosity() {
        return logVerbosity;
    }

    public static void setVerbosity(AnalyticsVerbosity verbosity) {
        logVerbosity = verbosity;
    }

    public enum DataType {
        String("java.lang.String");

        private String classPackage;

        private DataType(String classPackage) {
            this.classPackage = classPackage;

        }

        public String getClassType() {
            return classPackage;
        }
    }

}
