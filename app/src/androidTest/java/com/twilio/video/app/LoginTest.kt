package com.twilio.video.app

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.squareup.rx2.idler.Rx2Idler
import com.twilio.video.app.idlingresource.IdlingResourceModule
import com.twilio.video.app.ui.splash.SplashActivity
import io.reactivex.plugins.RxJavaPlugins
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginTest {

    @get:Rule
    var scenario = activityScenarioRule<SplashActivity>()

    @Test
    fun `it_should_login_successfully_with_email_and_then_logout`() {
            loginWithEmail()
            logout()
            onView(withId(R.id.google_sign_in_button)).check(matches(isDisplayed()))
    }

    private fun loginWithEmail() {
        onView(withId(R.id.email_sign_in_button)).perform(click())
        onView(withId(R.id.email_edittext)).perform(typeText("androidvideoappuitest@twilio.com"))
        onView(withId(R.id.password_edittext)).perform(typeText("P@\$\$W0rd"))
        onView(withId(R.id.login_button)).perform(click())
    }

    private fun logout() {
        getInstrumentation().targetContext.run {
            openActionBarOverflowOrOptionsMenu(this)
            onView(withText(getString(R.string.settings))).perform(click())
            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(actionOnItem<ViewHolder>(hasDescendant(withText(getString(R.string.settings_screen_logout))), click()))
        }
    }
}