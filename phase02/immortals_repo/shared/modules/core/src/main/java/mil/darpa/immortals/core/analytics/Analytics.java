package mil.darpa.immortals.core.analytics;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Analytics logging interface
 * <p>
 * Created by awellman@bbn.com on 3/22/16.
 */
public class Analytics {
    
    private static Logger logger = LoggerFactory.getLogger(Analytics.class);
    
    protected static final Gson gson = new Gson();
    private static HashMap<Long, Analytics> threadAnalyticsMap = new HashMap<>();

    private AnalyticsEndpointInterface endpoint;
    private AnalyticsVerbosity logVerbosity = AnalyticsVerbosity.Metadata;
    private String sourceIdentifier = "UNDEFINED";

    private LinkedList<AnalyticsEvent> logQueue;
    
    public synchronized static void registerCurrentThread() {
        long cid = Thread.currentThread().getId();
        if (threadAnalyticsMap.containsKey(cid)) {
            logger.warn("Analytics has already been registered on the current thread!");
            return;
        }
        logger.trace("Registering new Analytics instance for thread " + cid);
        Analytics a = new Analytics();
        threadAnalyticsMap.put(cid, a);
    }

    public synchronized static void registerThread(Thread thread) {
        long cid = Thread.currentThread().getId();

        if (!threadAnalyticsMap.containsKey(cid)) {
            throw new RuntimeException("Could not find an analytics instance tied to thread identifier '" + cid + "'!");
        }
        logger.trace("Registering Analytics instance from thread " + cid + " to thread " + thread.getId());
        threadAnalyticsMap.put(thread.getId(), threadAnalyticsMap.get(cid));
    }

    private Analytics() {
    }

    public static Analytics getInstance() {
        Long tid = Thread.currentThread().getId();
        if (!threadAnalyticsMap.containsKey(tid)) {
            throw new RuntimeException("Can not find an Analytics instance associated with this thread! If you would " +
                    "like to use Analytics, please execute Analytics.registerThread(Thread t) on the thread before starting it!");
        }
        return threadAnalyticsMap.get(tid);
    }

    // TODO: Use this re reuse events instead of unnecessarily create new objects
    public static AnalyticsEvent newEvent(@Nonnull AnalyticsEventType type) {
        return getInstance().newAnalyticsEvent(type, null, null);
    }

    public static AnalyticsEvent newEvent(@Nonnull AnalyticsEventType type, @Nonnull Object data) {
        return getInstance().newAnalyticsEvent(type, null, data);
    }

    public static AnalyticsEvent newEvent(@Nonnull AnalyticsEventType type, @Nonnull String remoteEventSource, @Nonnull Object data) {
        return getInstance().newAnalyticsEvent(type, remoteEventSource, data);
    }

    public AnalyticsEvent newAnalyticsEvent(@Nonnull AnalyticsEventType type, @Nullable String remoteEventSource, @Nullable Object data) {
        return new AnalyticsEvent(
                type,
                sourceIdentifier,
                remoteEventSource == null ? sourceIdentifier : remoteEventSource,
                data,
                System.currentTimeMillis()
        );
    }

    public synchronized static void initializeEndpoint(AnalyticsEndpointInterface endpointInterface) {
        long cid = Thread.currentThread().getId();
        Analytics a;
        
        if (threadAnalyticsMap.containsKey(cid)) {
            a = threadAnalyticsMap.get(cid);
            if (a.endpoint != null) {
                throw new RuntimeException("Analytics endpoint has already been initialized!");
            }
        } else {
            registerCurrentThread();
            a = getInstance();
            a.endpoint = endpointInterface;
        }

        a.endpoint = endpointInterface;
        a.endpoint.start();
        if (a.logQueue != null && !a.logQueue.isEmpty()) {
            for (AnalyticsEvent e : a.logQueue) {
                a.endpoint.log(e);
            }
        }
        if (a.logQueue != null) {
            a.logQueue.clear();
        }
    }

    @SuppressWarnings("unused")
    public synchronized static void shutdown() {
        AnalyticsEndpointInterface endpoint = getInstance().endpoint;
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
        getInstance().logEvent(analyticsEvent);
    }

    public synchronized void logEvent(@Nonnull AnalyticsEvent analyticsEvent) {
        if (endpoint == null) {
            if (logQueue == null) {
                logQueue = new LinkedList<>();
            }
            logQueue.add(analyticsEvent);

        } else {
            endpoint.log(analyticsEvent);
        }
    }

    public static void setSourceIdentifier(@Nonnull String identifier) {
        getInstance().sourceIdentifier = identifier;
    }

    @SuppressWarnings("unused")
    public static String getOwnSourceIdentifier() {
        return getInstance().sourceIdentifier;
    }

    @SuppressWarnings("unused")
    public static AnalyticsVerbosity getVerbosity() {
        return getInstance().logVerbosity;
    }

    public static void setVerbosity(AnalyticsVerbosity verbosity) {
        getInstance().logVerbosity = verbosity;
    }

    @SuppressWarnings("unused")
    public enum DataType {
        String("java.lang.String");

        private final String classPackage;

        DataType(String classPackage) {
            this.classPackage = classPackage;

        }

        public String getClassType() {
            return classPackage;
        }
    }

}
