package com.twilio.video.app.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.twilio.video.Video
import com.twilio.video.app.BuildConfig
import com.twilio.video.app.R
import com.twilio.video.app.auth.Authenticator
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.ui.ScreenSelector
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SettingsFragment : BaseSettingsFragment() {

    @Inject
    internal lateinit var screenSelector: ScreenSelector
    @Inject
    internal lateinit var authenticator: Authenticator

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Add our preference from resources
        addPreferencesFromResource(R.xml.preferences)

        setHasOptionsMenu(true)

        val versionCode = BuildConfig.VERSION_CODE.toString()
        findPreference<Preference>(Preferences.VERSION_NAME)?.summary = "${BuildConfig.VERSION_NAME} ($versionCode)"
        findPreference<Preference>(Preferences.VIDEO_LIBRARY_VERSION)?.summary = Video.getVersion()
        findPreference<Preference>(Preferences.LOGOUT)?.onPreferenceClickListener = Preference.OnPreferenceClickListener { logout(); true }
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
