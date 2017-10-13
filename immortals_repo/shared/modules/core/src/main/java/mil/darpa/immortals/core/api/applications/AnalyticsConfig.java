package mil.darpa.immortals.core.api.applications;

import mil.darpa.immortals.core.analytics.AnalyticsVerbosity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by awellman@bbn.com on 9/8/17.
 */
public class AnalyticsConfig {

    public AnalyticsTarget target;
    public AnalyticsVerbosity verbosity;
    public String url;
    public int port;

    public AnalyticsConfig(@Nonnull AnalyticsTarget target, @Nonnull AnalyticsVerbosity verbosity,
                           @Nullable String url, int port) {
        this.target = target;
        this.verbosity = verbosity;
        this.url = url;
        this.port = port;
    }
}
