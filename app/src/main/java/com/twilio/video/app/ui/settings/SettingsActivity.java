/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video.app.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.twilio.video.AudioCodec;
import com.twilio.video.Video;
import com.twilio.video.VideoCodec;
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

            setupListPreference(VideoCodec.class,
                    Preferences.VIDEO_CODEC,
                    Preferences.VIDEO_CODEC_DEFAULT,
                    (ListPreference) findPreference(Preferences.VIDEO_CODEC));

            setupListPreference(AudioCodec.class,
                    Preferences.AUDIO_CODEC,
                    Preferences.AUDIO_CODEC_DEFAULT,
                    (ListPreference) findPreference(Preferences.AUDIO_CODEC));

            // Fill out the rest of settings
            String identity = sharedPreferences.getString(Preferences.DISPLAY_NAME, null);
            findPreference(Preferences.IDENTITY).setSummary(identity);
            findPreference(Preferences.VERSION).setSummary(BuildConfig.VERSION_NAME);
            findPreference(Preferences.VIDEO_LIBRARY_VERSION).setSummary(Video.getVersion());
            findPreference(Preferences.LOGOUT).setOnPreferenceClickListener(logoutClickListener);
        }

        /**
         * Setup {@link ListPreference} from enums.
         *
         * @param enumClass    Enum class to obtain entries and entryValues from.
         * @param key          key to save with in {@link SharedPreferences}.
         * @param defaultValue default value of preference.
         * @param preference   instance of {@link ListPreference} to enum item to.
         * @param <T>          enum type.
         */
        private <T extends Enum<T>> void setupListPreference(Class<T> enumClass,
                                                             String key,
                                                             String defaultValue,
                                                             ListPreference preference) {

            // collection of all available values
            final String[] codecEntries = FluentIterable
                    .of(enumClass.getEnumConstants())
                    .transform(new Function<T, String>() {
                        @javax.annotation.Nullable
                        @Override
                        public String apply(@javax.annotation.Nullable T input) {
                            return input != null ? input.toString() : "null";
                        }
                    })
                    .toArray(String.class);

            // saved value
            final String value = sharedPreferences.getString(key, defaultValue);

            // bind values
            preference.setEntries(codecEntries);
            preference.setEntryValues(codecEntries);
            preference.setValue(value);
        }
    }
}
