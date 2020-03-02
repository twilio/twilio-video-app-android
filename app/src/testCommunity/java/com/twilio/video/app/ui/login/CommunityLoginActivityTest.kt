package com.twilio.video.app.ui.login

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.video.app.ApplicationModule
import com.twilio.video.app.TestApp
import com.twilio.video.app.DaggerCommunityIntegrationTestComponent
import com.twilio.video.app.auth.FirebaseWrapper
import com.twilio.video.app.auth.GoogleAuthProviderWrapper
import com.twilio.video.app.auth.GoogleAuthWrapper
import com.twilio.video.app.auth.GoogleSignInOptionsWrapper
import com.twilio.video.app.auth.GoogleSignInWrapper
import com.twilio.video.app.screen.assertLoadingIndicatorIsDisplayed
import com.twilio.video.app.screen.clickLoginButton
import com.twilio.video.app.screen.enterYourName
import com.twilio.video.app.screen.enterYourPasscode
import com.twilio.video.app.ui.room.RoomActivity
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(application = TestApp::class)
class CommunityLoginActivityTest {

    private lateinit var scenario: ActivityScenario<CommunityLoginActivity>
    private val testApp = ApplicationProvider.getApplicationContext<TestApp>()
    private val googleSignInActivityRequest = Intent(testApp, LoginActivityTest.TestActivity::class.java)
    private val googleAuthProviderWrapper: GoogleAuthProviderWrapper = mock()
    private val firebaseAuth: FirebaseAuth = mock()
    private val firebaseWrapper: FirebaseWrapper = mock {
        whenever(mock.instance).thenReturn(firebaseAuth)
    }
    private val googleAuthWrapper: GoogleAuthWrapper = mock()
    private val googleSignInClient: GoogleSignInClient = mock {
        whenever(mock.signInIntent).thenReturn(googleSignInActivityRequest)
    }
    private val googleSignInWrapper: GoogleSignInWrapper = mock {
        whenever(mock.getClient(any(), any())).thenReturn(googleSignInClient)
    }
    private val googleSignInOptionsWrapper: GoogleSignInOptionsWrapper = mock()

    @Before
    fun setUp() {
        val component = DaggerCommunityIntegrationTestComponent
                .builder()
                .applicationModule(ApplicationModule(testApp))
                .build()
        component.inject(testApp)

        scenario = ActivityScenario.launch(CommunityLoginActivity::class.java)
    }

    @Test
    fun `it should finish the login flow when auth is successful`() {
        enterYourName("TestUser")
        enterYourPasscode("0123456789")
        clickLoginButton()

        assertLoadingIndicatorIsDisplayed()

        // TODO Do something to return auth response

        val roomActivityRequest = Shadows.shadowOf(testApp).nextStartedActivity
        assertThat(roomActivityRequest.component, equalTo(Intent(testApp, RoomActivity::class.java).component))
    }

    @Test
    fun `it should display an error message when the user doesn't provide their name`() {
        TODO("not implemented")
    }

    @Test
    fun `it should display an error message when the user doesn't provide a passcode`() {
        TODO("not implemented")
    }

    @Test
    fun `it should display an error message when the auth request fails from an invalid passcode`() {
        TODO("not implemented")
    }

    @Test
    fun `it should display an error message when the auth request fails from an expired passcode`() {
        TODO("not implemented")
    }

    @Test
    fun `it should display an error message when the auth request fails for an unknown reason`() {
        TODO("not implemented")
    }
}
