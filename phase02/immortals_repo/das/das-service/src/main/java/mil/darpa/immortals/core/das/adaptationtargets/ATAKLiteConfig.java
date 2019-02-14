package mil.darpa.immortals.core.das.adaptationtargets;

import mil.darpa.immortals.core.analytics.AnalyticsVerbosity;

/**
 * Created by awellman@bbn.com on 2/9/16.
 */
// TODO: The DAS should not be aware of this file directly!
public class ATAKLiteConfig {

    public static transient final String CONFIG_FILE_NAME = "ataklite/ATAKLite-Config.json";

    public enum AnalyticsTarget {
        DEFAULT,
        STDOUT,
        NET_LOG4J
    }

    public static class AnalyticsConfig {

        public AnalyticsConfig() {
            target = AnalyticsTarget.DEFAULT;
            verbosity = AnalyticsVerbosity.Metadata;
        }

        public AnalyticsConfig(AnalyticsTarget target, AnalyticsVerbosity verbosity) {
            this.target = target;
            this.verbosity = verbosity;
        }

        public AnalyticsTarget target;
        public AnalyticsVerbosity verbosity;
        public String url;
        public int port;
    }

    public enum ConfigSource {
        SharedPreferences,
        Filesystem,
        Undefined
    }

    public enum UserInterface {
        Default,
        GoogleMaps,
        ListView
    }

    public static class ServerConfig {
        public String url;
        public Integer port;
    }

    public Boolean broadcastSA;

    public UserInterface userInterface;

    public Integer latestSABroadcastIntervalMS;

    public Integer latestSABroadcastDelayMS;

    public Integer imageBroadcastIntervalMS;

    public Integer imageBroadcastDelayMS;

    public ConfigSource configSource = ConfigSource.Undefined;

    public String callsign;

    public ServerConfig serverConfig;

    public AnalyticsConfig analyticsConfig = new AnalyticsConfig();

    public boolean logReceivedLocationUpdates;
    public boolean logOwnLocationUpdates;
    public boolean loadReceivedLocationUpdatesFromLog;
    public boolean loadOwnLocationUpdatesFromLog;
    public String locationLogExternalStoragePath = "ataklite/LocationLog.json";
}
