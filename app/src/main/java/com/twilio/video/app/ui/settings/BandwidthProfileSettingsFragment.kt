package com.twilio.video.app.ui.settings

import android.os.Bundle
import com.twilio.video.app.R
import com.twilio.video.app.data.Preferences

class BandwidthProfileSettingsFragment : BaseSettingsFragment() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.bandwidth_profile_preferences)

        setHasOptionsMenu(true)

        setListPreferenceValue(R.array.settings_screen_bandwidth_profile_mode_values,
            Preferences.BANDWIDTH_PROFILE_MODE,
            Preferences.BANDWIDTH_PROFILE_MODE_DEFAULT)
        setNumberPreferenceValue(Preferences.BANDWIDTH_PROFILE_MAX_SUBSCRIPTION_BITRATE,
            Preferences.BANDWIDTH_PROFILE_MAX_SUBSCRIPTION_BITRATE_DEFAULT)
        setNumberPreferenceValue(Preferences.BANDWIDTH_PROFILE_MAX_VIDEO_TRACKS,
            Preferences.BANDWIDTH_PROFILE_MAX_VIDEO_TRACKS_DEFAULT)
        setListPreferenceValue(R.array.settings_screen_bandwidth_profile_dominant_speaker_priority_values,
            Preferences.BANDWIDTH_PROFILE_DOMINANT_SPEAKER_PRIORITY,
            Preferences.BANDWIDTH_PROFILE_DOMINANT_SPEAKER_PRIORITY_DEFAULT)
        setListPreferenceValue(R.array.settings_screen_bandwidth_profile_track_switch_mode_values,
            Preferences.BANDWIDTH_PROFILE_TRACK_SWITCH_OFF_MODE,
            Preferences.BANDWIDTH_PROFILE_TRACK_SWITCH_OFF_MODE_DEFAULT)
        setListPreferenceValue(R.array.settings_screen_bandwidth_profile_render_dimensions,
            Preferences.BANDWIDTH_PROFILE_LOW_TRACK_PRIORITY_RENDER_DIMENSIONS,
            Preferences.BANDWIDTH_PROFILE_LOW_TRACK_PRIORITY_RENDER_DIMENSIONS_DEFAULT)
        setListPreferenceValue(R.array.settings_screen_bandwidth_profile_render_dimensions,
            Preferences.BANDWIDTH_PROFILE_STANDARD_TRACK_PRIORITY_RENDER_DIMENSIONS,
            Preferences.BANDWIDTH_PROFILE_STANDARD_TRACK_PRIORITY_RENDER_DIMENSIONS_DEFAULT)
        setListPreferenceValue(R.array.settings_screen_bandwidth_profile_render_dimensions,
            Preferences.BANDWIDTH_PROFILE_HIGH_TRACK_PRIORITY_RENDER_DIMENSIONS,
            Preferences.BANDWIDTH_PROFILE_HIGH_TRACK_PRIORITY_RENDER_DIMENSIONS_DEFAULT)
    }
}
