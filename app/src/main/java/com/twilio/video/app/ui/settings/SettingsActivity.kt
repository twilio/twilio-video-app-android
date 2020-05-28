/*
 * Copyright (C) 2019 Twilio, Inc.
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

package com.twilio.video.app.ui.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.DialogFragment
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.twilio.video.AudioCodec
import com.twilio.video.G722Codec
import com.twilio.video.H264Codec
import com.twilio.video.IsacCodec
import com.twilio.video.OpusCodec
import com.twilio.video.PcmaCodec
import com.twilio.video.PcmuCodec
import com.twilio.video.Video
import com.twilio.video.VideoCodec
import com.twilio.video.Vp8Codec
import com.twilio.video.Vp9Codec
import com.twilio.video.app.BuildConfig
import com.twilio.video.app.R
import com.twilio.video.app.auth.Authenticator
import com.twilio.video.app.base.BaseActivity
import com.twilio.video.app.data.NumberPreference
import com.twilio.video.app.data.NumberPreferenceDialogFragmentCompat
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.get
import com.twilio.video.app.ui.ScreenSelector
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SettingsActivity : BaseActivity() {

    @Inject
    internal lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Add the preference fragment
        val settingsFragment = SettingsFragment()
        supportFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, settingsFragment)
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        @Inject
        internal lateinit var sharedPreferences: SharedPreferences
        @Inject
        internal lateinit var screenSelector: ScreenSelector
        @Inject
        internal lateinit var authenticator: Authenticator
        private var identityPreference: EditTextPreference? = null

        override fun onAttach(context: Context) {
            AndroidSupportInjection.inject(this)
            super.onAttach(context)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // Add our preference from resources
            addPreferencesFromResource(R.xml.preferences)

            setupCodecListPreference(
                    VideoCodec::class.java,
                    Preferences.VIDEO_CODEC,
                    Preferences.VIDEO_CODEC_DEFAULT,
                    findPreference(Preferences.VIDEO_CODEC) as ListPreference?)

            setupCodecListPreference(
                    AudioCodec::class.java,
                    Preferences.AUDIO_CODEC,
                    Preferences.AUDIO_CODEC_DEFAULT,
                    findPreference(Preferences.AUDIO_CODEC) as ListPreference?)

            // Fill out the rest of settings
            identityPreference = (findPreference(Preferences.DISPLAY_NAME) as EditTextPreference?)?.apply {
                summary = sharedPreferences.getString(Preferences.DISPLAY_NAME, null)
                setOnPreferenceChangeListener { _, newValue ->
                    summary = newValue as CharSequence?
                    true
                }
            }
            findPreference(Preferences.VERSION_NAME).summary = BuildConfig.VERSION_NAME
            (findPreference(Preferences.VERSION_CODE))?.let { preference ->
                preference.summary = BuildConfig.VERSION_CODE.toString()
            }
            findPreference(Preferences.VIDEO_LIBRARY_VERSION).summary = Video.getVersion()
            findPreference(Preferences.LOGOUT).onPreferenceClickListener = Preference.OnPreferenceClickListener { logout(); true }
            val bandwidthProfileModeDefaultIndex =
                    resources.getStringArray(R.array.settings_screen_bandwidth_profile_mode_values)
                            .indexOf(sharedPreferences.get(Preferences.BANDWIDTH_PROFILE_MODE,
                                    Preferences.BANDWIDTH_PROFILE_MODE_DEFAULT))
            (findPreference(Preferences.BANDWIDTH_PROFILE_MODE) as ListPreference)
                    .setValueIndex(bandwidthProfileModeDefaultIndex)
        }

        override fun onDisplayPreferenceDialog(preference: Preference?) {
            if (preference == null) {
                return
            }

            // show custom dialog preference
            if (preference is NumberPreference) {
                val dialogFragment: DialogFragment?
                dialogFragment = NumberPreferenceDialogFragmentCompat.newInstance(preference.key)

                if (dialogFragment != null) {
                    dialogFragment.setTargetFragment(this, 0)
                    dialogFragment.show(requireFragmentManager(), PREFERENCE_FRAGMENT_TAG)
                }
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
        }

        private fun setupCodecListPreference(
            codecClass: Class<*>,
            key: String,
            defaultValue: String,
            preference: ListPreference?
        ) {
            if (preference == null) {
                return
            }

            val codecEntries = if (codecClass == AudioCodec::class.java) AUDIO_CODEC_NAMES else VIDEO_CODEC_NAMES
            // saved value
            val value = sharedPreferences.getString(key, defaultValue)

            // bind values
            preference.entries = codecEntries
            preference.entryValues = codecEntries
            preference.value = value
            preference.summary = value
            preference.setOnPreferenceChangeListener { changedPreference, newValue ->
                changedPreference.summary = newValue.toString()
                true
            }
        }

        private fun logout() {
            requireActivity().let { activity ->
                val loginIntent = Intent(activity, screenSelector.loginScreen)

                // Clear all preferences and set defaults
                sharedPreferences.edit().clear().apply()
                PreferenceManager.setDefaultValues(activity, R.xml.preferences, true)

                // Return to login activity
                loginIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                authenticator.logout()
                startActivity(loginIntent)
                activity.finishAffinity()
            }
        }

        companion object {
            private val PREFERENCE_FRAGMENT_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG"
        }
    }

    companion object {
        private val VIDEO_CODEC_NAMES = arrayOf(Vp8Codec.NAME, H264Codec.NAME, Vp9Codec.NAME)

        private val AUDIO_CODEC_NAMES = arrayOf(IsacCodec.NAME, OpusCodec.NAME, PcmaCodec.NAME, PcmuCodec.NAME, G722Codec.NAME)
    }
}
