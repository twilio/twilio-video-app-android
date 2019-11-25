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

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.DialogFragment
import androidx.preference.*
import com.twilio.video.*
import com.twilio.video.app.BuildConfig
import com.twilio.video.app.R
import com.twilio.video.app.auth.Authenticator
import com.twilio.video.app.base.BaseActivity
import com.twilio.video.app.data.NumberPreference
import com.twilio.video.app.data.NumberPreferenceDialogFragmentCompat
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.ui.ScreenSelector
import javax.inject.Inject

class SettingsActivity : BaseActivity() {

    @Inject
    internal lateinit var sharedPreferences: SharedPreferences
    @Inject
    internal lateinit var screenSelector: ScreenSelector
    @Inject
    internal lateinit var authenticator: Authenticator

    private val logoutClickListener = Preference.OnPreferenceClickListener { preference ->
        logout()
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Add the preference fragment
        val settingsFragment = SettingsFragment.newInstance(sharedPreferences, logoutClickListener)
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

    private fun logout() {
        val loginIntent = Intent(this, screenSelector.loginScreen)

        // Clear all preferences and set defaults
        sharedPreferences.edit().clear().apply()
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true)

        // Return to login activity
        loginIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        authenticator.logout()
        startActivity(loginIntent)
        finishAffinity()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private lateinit var sharedPreferences: SharedPreferences
        private var identityPreference: EditTextPreference? = null
        private var logoutClickListener: Preference.OnPreferenceClickListener? = null

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
            findPreference(Preferences.VERSION).summary = BuildConfig.VERSION_NAME
            findPreference(Preferences.VIDEO_LIBRARY_VERSION).summary = Video.getVersion()
            findPreference(Preferences.LOGOUT).onPreferenceClickListener = logoutClickListener
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
                codecClass: Class<*>, key: String, defaultValue: String, preference: ListPreference?) {
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

        companion object {
            private val PREFERENCE_FRAGMENT_TAG = "android.support.v7.preference.PreferenceFragment.DIALOG"

            internal fun newInstance(
                    sharedPreferences: SharedPreferences,
                    logoutClickListener: Preference.OnPreferenceClickListener): SettingsFragment {
                val settingsFragment = SettingsFragment()

                settingsFragment.logoutClickListener = logoutClickListener
                settingsFragment.sharedPreferences = sharedPreferences

                return settingsFragment
            }
        }
    }

    companion object {
        private val VIDEO_CODEC_NAMES = arrayOf(Vp8Codec.NAME, H264Codec.NAME, Vp9Codec.NAME)

        private val AUDIO_CODEC_NAMES = arrayOf(IsacCodec.NAME, OpusCodec.NAME, PcmaCodec.NAME, PcmuCodec.NAME, G722Codec.NAME)
    }
}
