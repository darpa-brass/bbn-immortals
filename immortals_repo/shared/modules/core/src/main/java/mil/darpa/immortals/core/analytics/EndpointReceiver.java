package mil.darpa.immortals.core.analytics;

/**
 * Created by awellman@bbn.com on 8/31/17.
 */
public interface EndpointReceiver<R> {
    void receive(R data);
}
