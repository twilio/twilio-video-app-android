package com.twilio.video.app.ui.settings

import android.os.Bundle
import androidx.preference.ListPreference
import com.twilio.video.app.R
import com.twilio.video.app.data.Preferences

class InternalSettingsFragment : BaseSettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.internal_preferences)

        (findPreference(Preferences.ENVIRONMENT) as ListPreference?)?.run {
            value = sharedPreferences.getString(Preferences.ENVIRONMENT,
                    Preferences.ENVIRONMENT_DEFAULT)
        }

        (findPreference(Preferences.TOPOLOGY) as ListPreference?)?.run {
            value = sharedPreferences.getString(Preferences.TOPOLOGY,
                    Preferences.TOPOLOGY_DEFAULT)
        }

        setHasOptionsMenu(true)
    }
}
