package com.twilio.video.app.screen

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.twilio.video.app.R
import com.twilio.video.app.data.Preferences.BANDWIDTH_PROFILE_MAX_SUBSCRIPTION_BITRATE_DEFAULT
import com.twilio.video.app.data.Preferences.BANDWIDTH_PROFILE_MAX_VIDEO_TRACKS_DEFAULT
import com.twilio.video.app.data.Preferences.SERVER_DEFAULT
import com.twilio.video.app.util.getString
import com.twilio.video.app.util.getStringArray

fun clickBandwidthProfileSettings() {
    val bandwidthProfile = R.string.settings_screen_bandwidth_profile
    onView(withId(R.id.recycler_view))
            .perform(actionOnItem<ViewHolder>(hasDescendant(withText(bandwidthProfile)), click()))
}

fun assertDefaultBandwidthProfileSettings() {
    assertDefaultValue(getString(R.string.settings_screen_bandwidth_profile_mode),
            getStringArray(R.array.settings_screen_bandwidth_profile_modes)[1])

    assertDefaultValue(getString(R.string.settings_screen_max_subscription_bitrate),
            BANDWIDTH_PROFILE_MAX_SUBSCRIPTION_BITRATE_DEFAULT.toString())

    assertDefaultValue(getString(R.string.settings_screen_max_video_tracks),
            BANDWIDTH_PROFILE_MAX_VIDEO_TRACKS_DEFAULT.toString())

    assertDefaultValue(getString(R.string.settings_screen_bandwidth_profile_dominant_speaker_priority),
            getStringArray(R.array.settings_screen_bandwidth_profile_dominant_speaker_priorities)[2])

    assertDefaultValue(getString(R.string.settings_screen_bandwidth_profile_track_switch_mode),
            SERVER_DEFAULT)

    assertDefaultValue(getString(R.string.settings_screen_bandwidth_profile_low_track_priority),
            SERVER_DEFAULT)

    assertDefaultValue(getString(R.string.settings_screen_bandwidth_profile_standard_track_priority),
            SERVER_DEFAULT)

    assertDefaultValue(getString(R.string.settings_screen_bandwidth_profile_high_track_priority),
            SERVER_DEFAULT)
}

private fun assertDefaultValue(preferenceTitle: String, preferenceValue: String) {
    onView(withId(R.id.recycler_view))
            .perform(scrollTo<ViewHolder>(hasDescendant(withText(preferenceTitle))))
    onView(withText(preferenceTitle)).check(matches(hasSibling(withText(preferenceValue))))
}
