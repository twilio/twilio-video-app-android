package com.twilio.video.app.e2eTest

import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.twilio.video.app.screen.assertGoogleSignInButtonIsVisible
import com.twilio.video.app.screen.assertSignInErrorIsVisible
import com.twilio.video.app.screen.clickSettingsMenuItem
import com.twilio.video.app.screen.loginWithEmail
import com.twilio.video.app.screen.loginWithWrongEmailCreds
import com.twilio.video.app.screen.logout
import com.twilio.video.app.ui.splash.SplashActivity
import com.twilio.video.app.util.retrieveEmailCredentials
import com.twilio.video.app.util.retryEspressoAction
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
        val emailCredentials = retrieveEmailCredentials()
        loginWithEmail(emailCredentials)
        retryEspressoAction { clickSettingsMenuItem() }
        logout()

        retryEspressoAction { assertGoogleSignInButtonIsVisible() }
    }

    @Test
    fun `it_should_not_login_successfully_with_email`() {
        val emailCredentials = retrieveEmailCredentials()
        loginWithWrongEmailCreds(emailCredentials)
        retryEspressoAction { assertSignInErrorIsVisible() }
    }
}
