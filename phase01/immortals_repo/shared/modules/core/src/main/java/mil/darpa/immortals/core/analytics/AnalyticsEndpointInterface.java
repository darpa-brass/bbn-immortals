package mil.darpa.immortals.core.analytics;

/**
 * Created by awellman@bbn.com on 8/2/16.
 */
// TODO: This could probably be replaced with slf4j or another standard library...
public interface AnalyticsEndpointInterface {
    void log(String eventPayload);

    void shutdown();
}
