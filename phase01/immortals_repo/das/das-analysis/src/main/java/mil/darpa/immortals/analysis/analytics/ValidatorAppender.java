package mil.darpa.immortals.analysis.analytics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.List;

/**
 * Created by awellman@bbn.com on 8/2/16.
 */
public abstract class ValidatorAppender extends AppenderSkeleton {

    private static Gson gson;

    static {
        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
    }

    @Override
    protected void append(LoggingEvent event) {
        processEvent(gson.fromJson(event.getRenderedMessage(), AnalyticsEvent.class));
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    protected abstract void processEvent(AnalyticsEvent event);

    public abstract String getValidatorName();
    public abstract boolean validate();
    public abstract List<String> getValidationErrors();

    public interface ValidationCompletionCallbackInterface {
        void validationSucceeded();
        void validationFailed();
        void validationSuccess(boolean validationFailed, String validationError);
    }
}
