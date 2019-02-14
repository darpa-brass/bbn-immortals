package com.bbn.ataklite;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;

public class ATAKLite extends Application {

    private static Context context;

    private static ATAKLiteConfig configuration;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getCustomAppContext() {
        return context;
    }

    public static String getSharedPreference(String key) {
        return PreferenceManager.getDefaultSharedPreferences(getCustomAppContext()).getString(key, "");
    }

    public static ATAKLiteConfig getConfigInstance() {
        if (configuration == null) {
            configuration = ATAKLiteConfig.loadConfig(getCustomAppContext());
        }
        return configuration;
    }
}
