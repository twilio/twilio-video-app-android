package com.twilio.video.app.ui.settings

import android.os.Bundle
import com.twilio.video.app.R

class AudioSettingsFragment : BaseSettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.audio_preferences)

        setHasOptionsMenu(true)
    }
}
