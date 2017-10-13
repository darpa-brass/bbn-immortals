package com.bbn.ataklite;

import mil.darpa.immortals.core.analytics.AnalyticsVerbosity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by awellman@bbn.com on 2/9/16.
 */
public class ATAKLiteConfig {

    public static transient final String CONFIG_DIRECTORY = "clitak/";
    public static transient final String CONFIG_FILE_NAME = CONFIG_DIRECTORY + "ATAKLite-Config.json";
    
    private static Logger logger = LoggerFactory.getLogger(ATAKLiteConfig.class);

    public enum AnalyticsTarget {
        DEFAULT,
        STDOUT,
        NET_LOG4J,
        LOCAL_JSON_CONSUMER
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
    public String locationLogExternalStoragePath = CONFIG_DIRECTORY + "LocationLog.json";


    /**
     * Attempts to load the configuration from the default config file location
     *
     * @return The configuration if it exists, null if it does not
     */
    public static ATAKLiteConfig loadConfig() {
        return loadConfig(CONFIG_FILE_NAME);
    }

    public static ATAKLiteConfig loadConfig(String filepath) {
        try {
            File inputFile = new File(filepath);

            if (inputFile.exists()) {
                FileReader fr = new FileReader(inputFile);
                ATAKLiteConfig config = GsonHelper.createGsonInstance().fromJson(fr, ATAKLiteConfig.class);
                config.configSource = ConfigSource.Filesystem;
                return config;
            } else {
                String err = "Configuration file must exist at location '" + inputFile.getAbsolutePath() + "'!";
                logger.error(err);
                throw new RuntimeException(err);
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
