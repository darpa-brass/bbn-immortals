package mil.darpa.immortals.analysis.analytics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 7/27/16.
 */
public class FormatConverter {

    private static final Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
    }

    public static final AnalyticsEvent StringToEvent(@Nonnull String eventString) {
        return gson.fromJson(eventString, AnalyticsEvent.class);

    }
}
