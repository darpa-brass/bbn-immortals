package mil.darpa.immortals.analytics.protocols.log4j;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.core.analytics.AnalyticsEndpointInterface;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Created by awellman@bbn.com on 1/18/17.
 */
public class Log4jAnalyticsEndpointAppender extends AppenderSkeleton {

    private static final Gson gson = new GsonBuilder().create();

    private final AnalyticsEndpointInterface aei;

    Log4jAnalyticsEndpointAppender(AnalyticsEndpointInterface endpoint) {
        this.aei = endpoint;
    }

    @Override
    protected void append(LoggingEvent event) {
        AnalyticsEvent ae = gson.fromJson(event.getRenderedMessage(), AnalyticsEvent.class);
        aei.log(ae);
    }

    @Override
    public void close() {
        // pass
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
