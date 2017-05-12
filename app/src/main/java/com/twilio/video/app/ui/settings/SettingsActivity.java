package com.twilio.video.app.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;

import com.twilio.video.Video;
import com.twilio.video.app.BuildConfig;
import com.twilio.video.app.R;
import com.twilio.video.app.auth.Authenticator;
import com.twilio.video.app.base.BaseActivity;
import com.twilio.video.app.data.Preferences;
import com.twilio.video.app.ui.login.LoginActivity;

import javax.inject.Inject;

public class SettingsActivity extends BaseActivity {
    @Inject SharedPreferences sharedPreferences;
    @Inject Authenticator authenticator;

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
        Intent loginIntent = new Intent(this, authenticator.getLoginActivity());

        // Clear all preferences and set defaults
        sharedPreferences.edit().clear().apply();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        // Invoke authenticator logout
        authenticator.logout();

        // Return to login activity
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        loginIntent.putExtra(LoginActivity.EXTRA_SIGN_OUT, true);
        startActivity(loginIntent);
        finishAffinity();
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
            String identity = sharedPreferences.getString(Preferences.DISPLAY_NAME, null);
            findPreference(Preferences.IDENTITY).setSummary(identity);
            findPreference(Preferences.VERSION).setSummary(BuildConfig.VERSION_NAME);
            findPreference(Preferences.VIDEO_LIBRARY_VERSION).setSummary(Video.getVersion());
            findPreference(Preferences.LOGOUT).setOnPreferenceClickListener(logoutClickListener);
        }
    }
}
