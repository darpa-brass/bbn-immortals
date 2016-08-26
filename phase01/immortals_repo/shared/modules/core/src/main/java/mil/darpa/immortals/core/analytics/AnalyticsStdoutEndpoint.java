package mil.darpa.immortals.core.analytics;

/**
 * Created by awellman@bbn.com on 8/2/16.
 */
public class AnalyticsStdoutEndpoint implements AnalyticsEndpointInterface {

    public AnalyticsStdoutEndpoint() {

    }

    @Override
    public void log(String eventPayload) {
        System.out.println(eventPayload);
    }

    @Override
    public void shutdown() {
        System.out.flush();
    }
}
