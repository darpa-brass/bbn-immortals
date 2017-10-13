package mil.darpa.immortals.core.api.ll.phase1;

/**
 * Created by awellman@bbn.com on 9/11/17.
 */
public class AnalyticsEvent {
    public final String type;
    public final String eventSource;
    public final long eventTime;
    public final String eventRemoteSource;
    public final String dataType;
    public final int eventId;
    public final String data;

    public AnalyticsEvent(String type, String eventSource, long eventTime, String eventRemoteSource, String dataType,
                          int eventId, String data) {
        this.type = type;
        this.eventSource = eventSource;
        this.eventTime = eventTime;
        this.eventRemoteSource = eventRemoteSource;
        this.dataType = dataType;
        this.eventId = eventId;
        this.data = data;
    }
}
