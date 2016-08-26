package mil.darpa.immortals.core.analytics;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by awellman@bbn.com on 8/2/16.
 */
public class AnalyticsEvent {
    private static final AtomicLong eventIdCounter = new AtomicLong(0);

    public AnalyticsEventType type;
    public final String eventSource;
    public String eventRemoteSource;
    public String dataType;
    public Object data;
    public long eventId;

    protected AnalyticsEvent(@Nonnull AnalyticsEventType type, @Nonnull String eventSource, @Nonnull String eventRemoteSource, @Nonnull Object data) {
        this.eventSource = eventSource;
        update(type, eventRemoteSource, data);
    }

    private void update(@Nonnull AnalyticsEventType type, @Nonnull String eventRemoteSource, @Nonnull Object data) {
        this.type = type;
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
}
