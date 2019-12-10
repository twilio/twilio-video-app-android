package com.twilio.video.app.test

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.twilio.video.app.EmailCredentials
import com.twilio.video.app.R
import com.twilio.video.app.TestCredentials
import com.twilio.video.app.retryViewMatcher
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
            loginWithEmail()
            logout()
            onView(withId(R.id.google_sign_in_button)).check(matches(isDisplayed()))
    }

    private fun retrieveEmailCredentials() {
        val reader = InputStreamReader(getInstrumentation().context.assets.open("Credentials/TestCredentials.json"))
        val jsonReader = JsonReader(reader)
        emailCredentials = (Gson().fromJson(jsonReader, TestCredentials::class.java) as TestCredentials).email_sign_in_user
    }

    private fun loginWithEmail() {
        onView(withId(R.id.email_sign_in_button)).perform(click())
        onView(withId(R.id.email_edittext)).perform(typeText(emailCredentials.email))
        onView(withId(R.id.password_edittext)).perform(typeText(emailCredentials.password))
        onView(withId(R.id.login_button)).perform(click())
    }

    private fun logout() {
        getInstrumentation().targetContext.run {
            retryViewMatcher({ openActionBarOverflowOrOptionsMenu(this) })
            onView(withText(getString(R.string.settings))).perform(click())
            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(actionOnItem<ViewHolder>(hasDescendant(withText(getString(R.string.settings_screen_logout))), click()))
        }
    }
}