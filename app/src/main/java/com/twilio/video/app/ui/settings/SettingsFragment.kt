package com.twilio.video.app.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
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
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.ui.ScreenSelector
import javax.inject.Inject

class SettingsFragment : BaseSettingsFragment() {

    @Inject
    internal lateinit var screenSelector: ScreenSelector
    @Inject
    internal lateinit var authenticator: Authenticator
    private var identityPreference: EditTextPreference? = null
    private val videoCodecNames = arrayOf(Vp8Codec.NAME, H264Codec.NAME, Vp9Codec.NAME)
    private val audioCodecNames = arrayOf(IsacCodec.NAME, OpusCodec.NAME, PcmaCodec.NAME, PcmuCodec.NAME, G722Codec.NAME)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Add our preference from resources
        addPreferencesFromResource(R.xml.preferences)

        setHasOptionsMenu(true)

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

        val codecEntries = if (codecClass == AudioCodec::class.java) audioCodecNames else videoCodecNames
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
}