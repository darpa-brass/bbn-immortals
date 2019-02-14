package com.bbn.ataklite;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import com.bbn.ataklite.service.SACommunicationService;

public class SettingsActivity extends Activity {

    private PrefsListener prefsListener = new PrefsListener();
    private PrefsFragment prefsFragment = new PrefsFragment();

    private String keyServerHost;
    private String keyServerPort;
    private String keyServerSAServiceState;
    private String keyServerClientId;

    @Override
    protected void onPause() {
        super.onPause();
        if (prefsFragment != null) {
            prefsFragment.getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(prefsListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefsFragment != null) {

            Resources res = this.getResources();
            keyServerHost = res.getString(R.string.key_pref_server_config_host);
            keyServerPort = res.getString(R.string.key_pref_server_config_port);
            keyServerSAServiceState = res.getString(R.string.key_pref_server_config_sa_service_state);
            keyServerClientId = res.getString(R.string.key_pref_client_config_id);

            prefsFragment.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(prefsListener);

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

            Preference connectionPref = prefsFragment.findPreference(keyServerHost);
            String label = getString(R.string.pref_server_config_host_summary);
            connectionPref.setSummary(label + ": " + sharedPref.getString(keyServerHost, ""));

            connectionPref = prefsFragment.findPreference(keyServerPort);
            label = getString(R.string.pref_server_config_port_summary);
            connectionPref.setSummary(label + ": " + sharedPref.getString(keyServerPort, ""));

            connectionPref = prefsFragment.findPreference(keyServerSAServiceState);
            label = getString(R.string.pref_server_config_sa_service_state_summary);
            boolean serviceState = sharedPref.getBoolean(keyServerSAServiceState, false);
            if (serviceState) {
                connectionPref.setSummary("Situation Awareness (SA) Services Are Enabled");
            } else {
                connectionPref.setSummary("Situation Awareness (SA) Services Are Disabled");
            }

            connectionPref = prefsFragment.findPreference(keyServerClientId);
            label = getString(R.string.pref_client_config_id_summary);
            connectionPref.setSummary(label + ": " + sharedPref.getString(keyServerClientId, ""));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, prefsFragment).commit();
    }

    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    public String getPreferenceValue(String key) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getString(key, "");
    }


    public class PrefsListener implements OnSharedPreferenceChangeListener {

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            String label = null;

            if (prefsFragment != null) {
                Preference connectionPref = prefsFragment.findPreference(key);

                if (key.equals(keyServerHost)) {
                    label = getString(R.string.pref_server_config_host_summary);
                    connectionPref.setSummary(label + ": " + sharedPreferences.getString(key, ""));
                } else if (key.equals(keyServerPort)) {
                    label = getString(R.string.pref_server_config_port_summary);
                    connectionPref.setSummary(label + ": " + sharedPreferences.getString(key, ""));
                } else if (key.equals(keyServerSAServiceState)) {
                    label = getString(R.string.pref_server_config_sa_service_state_summary);
                    connectionPref.setSummary(label + ": " + sharedPreferences.getBoolean(key, false));
                    if (sharedPreferences.getBoolean(key, false)) {
                        startService(new Intent(getBaseContext(), SACommunicationService.class));
                    } else {
                        stopService(new Intent(getBaseContext(), SACommunicationService.class));
                    }
                } else if (key.equals(keyServerClientId)) {
                    label = getString(R.string.pref_client_config_id_title);
                    connectionPref.setSummary(label + ": " + sharedPreferences.getString(key, ""));
                }
            }
        }
    }

}
