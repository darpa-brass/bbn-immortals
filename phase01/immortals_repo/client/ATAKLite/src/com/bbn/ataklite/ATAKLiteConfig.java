package com.bbn.ataklite;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;
import android.preference.PreferenceManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by awellman@bbn.com on 2/9/16.
 */
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
        }

        public AnalyticsConfig(AnalyticsTarget target) {
            this.target = target;
        }

        public AnalyticsTarget target;
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

    public static class TestSettings {
        public Integer imageBroadcastIntervalMS;
        public Integer imageBroadcastDelayMS;
    }

    public Boolean broadcastSA;

    public UserInterface userInterface;

    public Integer latestSABroadcastIntervalMS;

    public Integer latestSABroadcastDelayMS;

    public ConfigSource configSource = ConfigSource.Undefined;

    public String callsign;

    public ServerConfig serverConfig;

    public AnalyticsConfig analyticsConfig = new AnalyticsConfig();

    public TestSettings testSettings;

    public boolean logReceivedLocationUpdates;
    public boolean logOwnLocationUpdates;
    public boolean loadReceivedLocationUpdatesFromLog;
    public boolean loadOwnLocationUpdatesFromLog;
    public String locationLogExternalStoragePath = "ataklite/LocationLog.json";


    /**
     * Attempts to load the configuration from the default config file location
     *
     * @return The configuration if it exists, null if it does not
     */
    @Nullable
    private static ATAKLiteConfig loadFromConfigFile() {
        try {
            File inputFile = new File(Environment.getExternalStorageDirectory(), CONFIG_FILE_NAME);

            if (inputFile.exists()) {
                FileReader fr = new FileReader(inputFile);
                ATAKLiteConfig config = GsonHelper.createGsonInstance().fromJson(fr, ATAKLiteConfig.class);
                config.configSource = ConfigSource.Filesystem;
                return config;
            }

            return null;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Attempts to load the configuration from the Shared Preferences
     *
     * @param context Current Context
     * @return The base configuration configurable via the UI if it is configured, null otherwise.
     */
    @Nullable
    private static ATAKLiteConfig loadFromSharedPrefs(@Nonnull Context context) {

        Resources res = context.getResources();
        String keyServerHost = res.getString(R.string.key_pref_server_config_host);
        String keyServerPort = res.getString(R.string.key_pref_server_config_port);
        String keyServerSAServiceState = res.getString(R.string.key_pref_server_config_sa_service_state);
        String keyServerClientId = res.getString(R.string.key_pref_client_config_id);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        String takServerHost = sharedPref.getString(keyServerHost, null);
        String takServerPort = sharedPref.getString(keyServerPort, null);
        Boolean takServerSAServiceState = sharedPref.getBoolean(keyServerSAServiceState, false);
        String takServerClientId = sharedPref.getString(keyServerClientId, null);

        if (takServerHost != null && takServerPort != null && takServerClientId != null && takServerSAServiceState != null) {
            ATAKLiteConfig config = new ATAKLiteConfig();

            config.callsign = takServerClientId;
            config.serverConfig = new ServerConfig();
            config.serverConfig.port = Integer.parseInt(takServerPort);
            config.serverConfig.url = takServerHost;
            config.configSource = ConfigSource.SharedPreferences;

            return config;
        }
        return null;
    }

    @Nullable
    public static ATAKLiteConfig loadConfig(@Nonnull Context context) {
        ATAKLiteConfig config = loadFromConfigFile();

        if (config == null) {
            config = loadFromSharedPrefs(context);
        }

        return config;
    }

}
