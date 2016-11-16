package mil.darpa.immortals.harness;

import com.google.gson.Gson;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;

/**
 * Created by awellman@bbn.com on 11/3/16.
 */
public abstract class AbstractMessageListener {

    private static final Gson gson = new Gson();

    public final void receiveMessage(String message) {
        AnalyticsEvent event = gson.fromJson(message, AnalyticsEvent.class);
        System.out.println("Break");
    }


}
