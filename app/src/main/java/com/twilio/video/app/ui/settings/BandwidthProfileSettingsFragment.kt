package com.twilio.video.app.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.twilio.video.app.R
import com.twilio.video.app.data.NumberPreference
import com.twilio.video.app.data.NumberPreferenceDialogFragmentCompat
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.get
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class BandwidthProfileSettingsFragment : PreferenceFragmentCompat() {

    @Inject
    internal lateinit var sharedPreferences: SharedPreferences

    private var previousTitle: CharSequence? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Add our preference from resources
        addPreferencesFromResource(R.xml.bandwidth_profile_preferences)

        val bandwidthProfileModeDefaultIndex =
            resources.getStringArray(R.array.settings_screen_bandwidth_profile_mode_values)
                .indexOf(sharedPreferences.get(
                    Preferences.BANDWIDTH_PROFILE_MODE,
                    Preferences.BANDWIDTH_PROFILE_MODE_DEFAULT))
        (findPreference(Preferences.BANDWIDTH_PROFILE_MODE) as ListPreference)
            .setValueIndex(bandwidthProfileModeDefaultIndex)
        (findPreference(Preferences.BANDWIDTH_PROFILE_MAX_SUBSCRIPTION_BITRATE) as NumberPreference).apply {
            val maxTracks = sharedPreferences
                .get(
                    Preferences.BANDWIDTH_PROFILE_MAX_SUBSCRIPTION_BITRATE,
                    Preferences.BANDWIDTH_PROFILE_MAX_SUBSCRIPTION_BITRATE_DEFAULT)
            summary = maxTracks.toString()
            number = maxTracks
        }
        (findPreference(Preferences.BANDWIDTH_PROFILE_MAX_VIDEO_TRACKS) as NumberPreference).apply {
            val maxTracks = sharedPreferences
                .get(
                    Preferences.BANDWIDTH_PROFILE_MAX_VIDEO_TRACKS,
                    Preferences.BANDWIDTH_PROFILE_MAX_VIDEO_TRACKS_DEFAULT)
            summary = maxTracks.toString()
            number = maxTracks
        }
        val bandwidthProfileDominantSpeakerPriorityDefaultIndex =
            resources.getStringArray(R.array.settings_screen_bandwidth_profile_dominant_speaker_priority_values)
                .indexOf(sharedPreferences.get(
                    Preferences.BANDWIDTH_PROFILE_DOMINANT_SPEAKER_PRIORITY,
                    Preferences.BANDWIDTH_PROFILE_DOMINANT_SPEAKER_PRIORITY_DEFAULT))
        (findPreference(Preferences.BANDWIDTH_PROFILE_DOMINANT_SPEAKER_PRIORITY) as ListPreference)
            .setValueIndex(bandwidthProfileDominantSpeakerPriorityDefaultIndex)
        val bandwidthProfileTrackSwitchOffModeDefaultIndex =
            resources.getStringArray(R.array.settings_screen_bandwidth_profile_track_switch_mode_values)
                .indexOf(sharedPreferences.get(
                    Preferences.BANDWIDTH_PROFILE_TRACK_SWITCH_OFF_MODE,
                    Preferences.BANDWIDTH_PROFILE_TRACK_SWITCH_OFF_MODE_DEFAULT))
        (findPreference(Preferences.BANDWIDTH_PROFILE_TRACK_SWITCH_OFF_MODE) as ListPreference)
            .setValueIndex(bandwidthProfileTrackSwitchOffModeDefaultIndex)
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
                dialogFragment.show(requireFragmentManager(), BANDWIDTH_PROFILE_PREFERENCE_FRAGMENT_TAG)
            }
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    companion object {
        private const val BANDWIDTH_PROFILE_PREFERENCE_FRAGMENT_TAG = "BANDWIDTH_PROFILE_PREFERENCE_FRAGMENT_TAG"
    }
}
