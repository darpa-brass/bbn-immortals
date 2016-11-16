package mil.darpa.immortals.core.analytics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by awellman@bbn.com on 8/2/16.
 */
public class AnalyticsEvent {
    private transient static final AtomicLong eventIdCounter = new AtomicLong(0);
    private transient static Gson _gson;

    public AnalyticsEventType type;
    public final String eventSource;
    public final String eventRemoteSource;
    public final String dataType;
    public final String data;
    public final long eventId;

    private static Gson getGson() {
        if (_gson == null) {
            _gson = new Gson();
        }
        return _gson;
    }

    protected AnalyticsEvent(@Nonnull AnalyticsEventType type, @Nonnull String eventSource, @Nonnull String eventRemoteSource, @Nonnull Object data) {
        this.type = type;
        this.eventSource = eventSource;
        this.eventRemoteSource = eventRemoteSource;
        this.eventId = eventIdCounter.getAndIncrement();

        if (Analytics.logVerbosity == AnalyticsVerbosity.Metadata) {
            this.dataType = null;
            this.data = null;

        } else if (Analytics.logVerbosity == AnalyticsVerbosity.Data) {
            this.dataType = data.getClass().getCanonicalName();
            this.data = Analytics.gson.toJson(data);

        } else {
            throw new RuntimeException("Unexpected verbosity level " + Analytics.logVerbosity.name());
        }
    }

    public String toString() {
        return getGson().toJson(this, AnalyticsEvent.class);
    }
}
