package com.twilio.video.app.e2eTest

import android.preference.PreferenceManager
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.api.model.Topology
import com.twilio.video.app.screen.assertSettingsTitleIsVisible
import com.twilio.video.app.screen.clickSettingsMenuItem
import com.twilio.video.app.ui.splash.SplashActivity
import com.twilio.video.app.util.getTargetContext
import com.twilio.video.app.util.retryEspressoAction
import com.twilio.video.app.util.uiDevice
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@Ignore
@RunWith(AndroidJUnit4::class)
@LargeTest
class PreferencesTest : BaseUITest() {

    @get:Rule
    var scenario = activityScenarioRule<SplashActivity>()

    @Test
    fun it_should_assert_correct_default_shared_preferences() {
        retryEspressoAction { clickSettingsMenuItem() }

        retryEspressoAction { assertSettingsTitleIsVisible() }

        uiDevice().run {
            pressBack()
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getTargetContext())
        assertThat(sharedPreferences.getString(Preferences.TOPOLOGY, null), equalTo(Topology.GROUP.string))
    }
}