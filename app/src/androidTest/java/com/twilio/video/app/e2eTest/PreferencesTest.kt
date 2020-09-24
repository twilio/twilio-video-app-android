package com.twilio.video.app.e2eTest

import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.twilio.video.app.R
import com.twilio.video.app.screen.assertDefaultBandwidthProfileSettings
import com.twilio.video.app.screen.clickSettingsMenuItem
import com.twilio.video.app.ui.splash.SplashActivity
import com.twilio.video.app.util.assertTextIsDisplayedRetry
import com.twilio.video.app.util.getString
import com.twilio.video.app.util.retryEspressoAction
import com.twilio.video.app.util.scrollAndClickView
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
@E2ETest
class PreferencesTest : BaseE2ETest() {

    @get:Rule
    var scenario = activityScenarioRule<SplashActivity>()

    @Test
    fun it_should_assert_correct_default_bandwidth_preferences() {
        retryEspressoAction { clickSettingsMenuItem() }

        assertTextIsDisplayedRetry(getString(R.string.settings_title))

        scrollAndClickView(getString(R.string.settings_title_advanced), R.id.recycler_view)

        assertTextIsDisplayedRetry(getString(R.string.settings_title_advanced))

        scrollAndClickView(getString(R.string.settings_title_bandwidth_profile), R.id.recycler_view)

        assertTextIsDisplayedRetry(getString(R.string.settings_title_bandwidth_profile))

        assertDefaultBandwidthProfileSettings()
    }
}
