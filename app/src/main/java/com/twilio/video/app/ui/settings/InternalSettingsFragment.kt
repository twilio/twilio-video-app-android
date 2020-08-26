package com.twilio.video.app.ui.settings

import android.os.Bundle
import androidx.preference.ListPreference
import com.twilio.video.app.R
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.api.model.Topology

class InternalSettingsFragment : BaseSettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.internal_preferences)

        findPreference<ListPreference>(Preferences.ENVIRONMENT)?.run {
            value = sharedPreferences.getString(Preferences.ENVIRONMENT,
                    Preferences.ENVIRONMENT_DEFAULT)
        }

        findPreference<ListPreference>(Preferences.TOPOLOGY)?.run {
            val roomTypes = Topology.values().map { it.value }.toTypedArray()
            entries = roomTypes
            entryValues = roomTypes
            value = sharedPreferences.getString(Preferences.TOPOLOGY,
                    Preferences.TOPOLOGY_DEFAULT)
        }

        setHasOptionsMenu(true)
    }
}
