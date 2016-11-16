package mil.darpa.immortals.analytics.protocols.log4j;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.analytics.validators.ValidatorManager;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Created by awellman@bbn.com on 10/26/16.
 */
public class ValidatorManagerLog4jAppender extends AppenderSkeleton {

    public final ValidatorManager validatorManager;

    private static final Gson gson;

    static {
        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
    }

    public ValidatorManagerLog4jAppender(ValidatorManager validationManager) {
        this.validatorManager = validationManager;
    }

    @Override
    protected void append(LoggingEvent event) {
        validatorManager.processEvent(gson.fromJson(event.getRenderedMessage(), AnalyticsEvent.class));
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
