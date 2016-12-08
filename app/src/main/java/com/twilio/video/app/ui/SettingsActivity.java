package com.twilio.video.app.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;

import com.twilio.video.app.BuildConfig;
import com.twilio.video.app.R;
import com.twilio.video.app.data.Preferences;
import com.twilio.video.env.Env;

public class SettingsActivity extends AppCompatActivity {
    private static final String TWILIO_ENV_KEY = "TWILIO_ENVIRONMENT";

    private SharedPreferences sharedPreferences;

    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(Preferences.REALM)) {
                String realm = sharedPreferences.getString(key, Preferences.REALM_DEFAULT);
                Env.set(SettingsActivity.this, TWILIO_ENV_KEY, realm, true);
            }
        }
    };

    private final Preference.OnPreferenceClickListener logoutClickListener =
            new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    logout();
                    return true;
                }
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register for preference changes
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        // Add the preference fragment
        SettingsFragment settingsFragment = SettingsFragment.newInstance(sharedPreferences,
                logoutClickListener);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, settingsFragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        Intent registrationIntent = new Intent(this, LoginActivity.class);

        // Clear all preferences and set defaults
        sharedPreferences.edit().clear().apply();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);


        // Return to login activity
        startActivity(registrationIntent);
        finish();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private SharedPreferences sharedPreferences;
        private Preference.OnPreferenceClickListener logoutClickListener;

        static SettingsFragment newInstance(SharedPreferences sharedPreferences,
                                            Preference.OnPreferenceClickListener logoutClickListener) {
            SettingsFragment settingsFragment = new SettingsFragment();

            settingsFragment.logoutClickListener = logoutClickListener;
            settingsFragment.sharedPreferences = sharedPreferences;

            return settingsFragment;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // Add our preference from resources
            addPreferencesFromResource(R.xml.preferences);

            // Fill out the rest of settings
            String identity = sharedPreferences.getString(Preferences.IDENTITY, null);
            findPreference(Preferences.IDENTITY).setSummary(identity);
            findPreference(Preferences.VERSION).setSummary(BuildConfig.VERSION_NAME);
            findPreference(Preferences.LOGOUT).setOnPreferenceClickListener(logoutClickListener);
        }
    }
}
