package com.twilio.video.app.integrationTest

import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.twilio.video.app.R
import com.twilio.video.app.screen.assertDefaultBandwidthProfileSettings
import com.twilio.video.app.ui.settings.SettingsActivity
import com.twilio.video.app.util.assertTextIsDisplayedRetry
import com.twilio.video.app.util.getString
import com.twilio.video.app.util.scrollAndClickView
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
@IntegrationTest
class PreferenceIntegrationTest {

    @get:Rule
    var scenario = activityScenarioRule<SettingsActivity>()

    @Test
    fun configuration_change_should_not_crash_app() {
        val title = getString(R.string.settings_title)
        assertTextIsDisplayedRetry(title)
        scenario.scenario.recreate()
        assertTextIsDisplayedRetry(title)
    }

    @Test
    fun it_should_assert_correct_default_bandwidth_preferences() {
        scrollAndClickView(getString(R.string.settings_title_advanced), R.id.recycler_view)

        assertTextIsDisplayedRetry(getString(R.string.settings_title_advanced))

        scrollAndClickView(getString(R.string.settings_title_bandwidth_profile), R.id.recycler_view)

        assertTextIsDisplayedRetry(getString(R.string.settings_title_bandwidth_profile))

        assertDefaultBandwidthProfileSettings()
    }
}
