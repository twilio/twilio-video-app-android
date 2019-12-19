package com.twilio.video.app.test

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.twilio.video.app.*
import com.twilio.video.app.screen.assertGoogleSignInButtonIsVisible
import com.twilio.video.app.screen.assertSignInErrorIsVisible
import com.twilio.video.app.screen.loginWithEmail
import com.twilio.video.app.screen.loginWithWrongEmailCreds
import com.twilio.video.app.ui.splash.SplashActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStreamReader

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginTest {

    @get:Rule
    var scenario = activityScenarioRule<SplashActivity>()
    private lateinit var emailCredentials: EmailCredentials

    @Test
    fun `it_should_login_successfully_with_email_and_then_logout`() {
        retrieveEmailCredentials()
        loginWithEmail(emailCredentials)
        logout()
        assertGoogleSignInButtonIsVisible()
    }

    @Test
    fun `it_should_not_login_successfully_with_email`() {
        retrieveEmailCredentials()
        loginWithWrongEmailCreds(emailCredentials)
        retryViewMatcher{ assertSignInErrorIsVisible() }
    }

    private fun retrieveEmailCredentials() {
        val reader = InputStreamReader(getInstrumentation().context.assets.open("Credentials/TestCredentials.json"))
        val jsonReader = JsonReader(reader)
        emailCredentials = (Gson().fromJson(jsonReader, TestCredentials::class.java) as TestCredentials).email_sign_in_user
    }

    private fun logout() {
        getInstrumentation().targetContext.run {
            retryViewMatcher { openActionBarOverflowOrOptionsMenu(this) }
            onView(withText(getString(R.string.settings))).perform(click())
            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(actionOnItem<ViewHolder>(hasDescendant(withText(getString(R.string.settings_screen_logout))), click()))
        }
    }
}