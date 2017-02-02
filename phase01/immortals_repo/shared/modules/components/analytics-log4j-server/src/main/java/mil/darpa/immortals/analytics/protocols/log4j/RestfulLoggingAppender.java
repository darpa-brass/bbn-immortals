package mil.darpa.immortals.analytics.protocols.log4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Created by awellman@bbn.com on 1/18/17.
 */
public class RestfulLoggingAppender extends AppenderSkeleton {
    private AnalyticsEventReporter analyticsEventReporter;

    public RestfulLoggingAppender(String address, int port) {
        AnalyticsEventReporter.initialize(address, port);
        analyticsEventReporter = AnalyticsEventReporter.getInstance();
    }

    @Override
    protected void append(LoggingEvent event) {
        analyticsEventReporter.report(event.getRenderedMessage());
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
