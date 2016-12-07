package com.twilio.video.app.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;

import com.twilio.video.app.R;
import com.twilio.video.app.data.Preferences;
import com.twilio.video.env.Env;

public class SettingsActivity extends AppCompatActivity {
    private static final String TWILIO_ENV_KEY = "TWILIO_ENVIRONMENT";
    private static final String DEFAULT_REALM = "Production";

    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(Preferences.REALM)) {
                String realm = sharedPreferences.getString(key, DEFAULT_REALM);
                Env.set(SettingsActivity.this, TWILIO_ENV_KEY, realm, true);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register for preference changes
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        // Add the preference fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
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

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
