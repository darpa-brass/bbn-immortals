package mil.darpa.immortals.core.analytics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by awellman@bbn.com on 8/2/16.
 */
public class AnalyticsStdoutEndpoint implements AnalyticsEndpointInterface {
    
    public static final Gson gson = new GsonBuilder().create();

    public AnalyticsStdoutEndpoint() {
    }

    @Override
    public void log(AnalyticsEvent analyticsEvent) {
        System.out.println(gson.toJson(analyticsEvent));
    }

    @Override
    public void start() {
        // pass
    }
    
    @Override
    public void shutdown() {
        System.out.flush();
    }
}
