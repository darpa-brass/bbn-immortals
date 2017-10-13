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

    private static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            if ((i == 0 && str.charAt(i) == '-' && str.length() == 1) ||
                    !Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static String removeImagePostfix(String eventSource) {
        int imagePostfixIdx = eventSource.indexOf("-i");
        if (imagePostfixIdx >= 0 && isInteger(eventSource.substring(imagePostfixIdx + 2))) {
            return eventSource.substring(0, imagePostfixIdx);
        }
        return eventSource;
    }

    AnalyticsEvent(@Nonnull AnalyticsEventType type, @Nonnull String eventSource, @Nullable String eventRemoteSource, @Nullable Object data, long eventTime) {
        AnalyticsVerbosity logVerbosity = Analytics.getVerbosity();
        
        this.type = type;
        this.eventId = eventIdCounter.getAndIncrement();
        this.eventTime = eventTime;

        // Image identifers are appended to the UID in this case
        if (type == AnalyticsEventType.MyImageSent || type == AnalyticsEventType.FieldImageReceived) {
            this.eventSource = removeImagePostfix(eventSource);
            this.eventRemoteSource = removeImagePostfix(eventRemoteSource);
        } else {
            this.eventSource = eventSource;
            this.eventRemoteSource = eventRemoteSource;
        }

        if (logVerbosity == AnalyticsVerbosity.Metadata) {
            this.dataType = null;
            this.data = null;

        } else if (logVerbosity == AnalyticsVerbosity.Data && data != null) {
            this.dataType = data.getClass().getCanonicalName();
            this.data = Analytics.gson.toJson(data);

        } else {
            throw new RuntimeException("Unexpected verbosity level " + logVerbosity.name());
        }
    }

    public String toString() {
        return getGson().toJson(this, AnalyticsEvent.class);
    }
}
