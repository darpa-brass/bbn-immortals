package mil.darpa.immortals.core.analytics;

/**
 * Created by awellman@bbn.com on 8/2/16.
 */
public interface AnalyticsEndpointInterface {
    void log(String eventPayload);

    void shutdown();
}
