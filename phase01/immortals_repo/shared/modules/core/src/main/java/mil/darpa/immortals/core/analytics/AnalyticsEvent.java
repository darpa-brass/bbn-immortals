package mil.darpa.immortals.core.analytics;

import com.google.gson.Gson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Analytics logging data format
 * <p>
 * Created by awellman@bbn.com on 8/2/16.
 */
@SuppressWarnings("WeakerAccess")
public class AnalyticsEvent {
    private transient static final AtomicLong eventIdCounter = new AtomicLong(0);
    private transient static Gson _gson;

    public final AnalyticsEventType type;
    public final String eventSource;
    public final String eventRemoteSource;
    public final String dataType;
    public final long eventTime;
    public final String data;
    public final long eventId;

    private static Gson getGson() {
        if (_gson == null) {
            _gson = new Gson();
        }
        return _gson;
    }

    public AnalyticsEvent(@Nonnull AnalyticsEventType type, @Nonnull String eventSource, @Nonnull String eventRemoteSource, @Nullable Object data, long eventTime) {
        this.type = type;
        this.eventId = eventIdCounter.getAndIncrement();
        this.eventTime = eventTime;

        // Image identifers are appended to the UID in this case
        if (type == AnalyticsEventType.MyImageSent || type == AnalyticsEventType.FieldImageReceived) {
            if (eventSource.endsWith("-i")) {
                this.eventSource = eventSource.substring(0, eventSource.length() - 2);
            } else {
                this.eventSource = eventSource;
            }

            if (eventRemoteSource.endsWith("-i")) {
                this.eventRemoteSource = eventRemoteSource.substring(0, eventRemoteSource.length() - 2);
            } else {
                this.eventRemoteSource = eventRemoteSource;

            }

        } else {
            this.eventSource = eventSource;
            this.eventRemoteSource = eventRemoteSource;

        }

        if (Analytics.logVerbosity == AnalyticsVerbosity.Metadata) {
            this.dataType = null;
            this.data = null;

        } else if (Analytics.logVerbosity == AnalyticsVerbosity.Data && data != null) {
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
