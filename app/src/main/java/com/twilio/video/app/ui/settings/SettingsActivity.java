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
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;
import com.twilio.video.AudioCodec;
import com.twilio.video.G722Codec;
import com.twilio.video.H264Codec;
import com.twilio.video.IsacCodec;
import com.twilio.video.OpusCodec;
import com.twilio.video.PcmaCodec;
import com.twilio.video.PcmuCodec;
import com.twilio.video.Video;
import com.twilio.video.VideoCodec;
import com.twilio.video.Vp8Codec;
import com.twilio.video.Vp9Codec;
import com.twilio.video.app.BuildConfig;
import com.twilio.video.app.R;
import com.twilio.video.app.auth.Authenticator;
import com.twilio.video.app.base.BaseActivity;
import com.twilio.video.app.data.NumberPreference;
import com.twilio.video.app.data.NumberPreferenceDialogFragmentCompat;
import com.twilio.video.app.data.Preferences;
import com.twilio.video.app.ui.login.LoginActivity;
import javax.inject.Inject;

public class SettingsActivity extends BaseActivity {
    private static final String[] VIDEO_CODEC_NAMES =
            new String[] {Vp8Codec.NAME, H264Codec.NAME, Vp9Codec.NAME};

    private static final String[] AUDIO_CODEC_NAMES =
            new String[] {
                IsacCodec.NAME, OpusCodec.NAME, PcmaCodec.NAME, PcmuCodec.NAME, G722Codec.NAME
            };

    @Inject SharedPreferences sharedPreferences;
    @Inject Authenticator authenticator;

    private final Preference.OnPreferenceClickListener logoutClickListener =
            preference -> {
                logout();
                return true;
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add the preference fragment
        SettingsFragment settingsFragment =
                SettingsFragment.newInstance(sharedPreferences, logoutClickListener);
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
        private static final String PREFERENCE_FRAGMENT_TAG =
                "android.support.v7.preference.PreferenceFragment.DIALOG";

        private SharedPreferences sharedPreferences;
        private Preference.OnPreferenceClickListener logoutClickListener;

        static SettingsFragment newInstance(
                SharedPreferences sharedPreferences,
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

            setupCodecListPreference(
                    VideoCodec.class,
                    Preferences.VIDEO_CODEC,
                    Preferences.VIDEO_CODEC_DEFAULT,
                    (ListPreference) findPreference(Preferences.VIDEO_CODEC));

            setupCodecListPreference(
                    AudioCodec.class,
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

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            if (preference == null) {
                return;
            }

            // show custom dialog preference
            if (preference instanceof NumberPreference) {
                DialogFragment dialogFragment;
                dialogFragment =
                        NumberPreferenceDialogFragmentCompat.newInstance(preference.getKey());

                if (dialogFragment != null) {
                    dialogFragment.setTargetFragment(this, 0);
                    dialogFragment.show(getFragmentManager(), PREFERENCE_FRAGMENT_TAG);
                }

            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }

        private void setupCodecListPreference(
                Class codecClass, String key, String defaultValue, ListPreference preference) {
            if (preference == null) {
                return;
            }

            String[] codecEntries =
                    (codecClass == AudioCodec.class) ? AUDIO_CODEC_NAMES : VIDEO_CODEC_NAMES;
            // saved value
            final String value = sharedPreferences.getString(key, defaultValue);

            // bind values
            preference.setEntries(codecEntries);
            preference.setEntryValues(codecEntries);
            preference.setValue(value);
            preference.setSummary(value);
            preference.setOnPreferenceChangeListener(
                    (changedPreference, newValue) -> {
                        changedPreference.setSummary(newValue.toString());
                        return true;
                    });
        }
    }
}
