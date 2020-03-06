package com.twilio.video.app.ui.login

import android.content.Intent
import android.preference.PreferenceManager
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.google.android.material.textfield.TextInputEditText
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.video.app.ApplicationModule
import com.twilio.video.app.DaggerCommunityIntegrationTestComponent
import com.twilio.video.app.R
import com.twilio.video.app.TestApp
import com.twilio.video.app.data.AuthServiceModule
import com.twilio.video.app.data.api.AuthService
import com.twilio.video.app.data.api.AuthServiceRepository
import com.twilio.video.app.data.api.AuthServiceRequestDTO
import com.twilio.video.app.data.api.AuthServiceResponseDTO
import com.twilio.video.app.data.api.URL_PREFIX
import com.twilio.video.app.data.api.URL_SUFFIX
import com.twilio.video.app.screen.assertLoadingIndicatorIsDisplayed
import com.twilio.video.app.screen.assertLoadingIndicatorIsNotDisplayed
import com.twilio.video.app.screen.clickLoginButton
import com.twilio.video.app.screen.enterYourName
import com.twilio.video.app.ui.room.RoomActivity
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
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
    private val authService: AuthService = mock()
    private val scheduler = TestScheduler()
    private val authServiceRepository = AuthServiceRepository(authService,
            PreferenceManager.getDefaultSharedPreferences(testApp),
            scheduler)
    private val authServiceModule: AuthServiceModule = mock {
        whenever(mock.providesOkHttpClient()).thenReturn(mock())
        whenever(mock.providesAuthService(any())).thenReturn(authService)
        whenever(mock.providesTokenService(any(), any())).thenReturn(authServiceRepository)
    }

    @Before
    fun setUp() {
        val component = DaggerCommunityIntegrationTestComponent
                .builder()
                .applicationModule(ApplicationModule(testApp))
                .authServiceModule(authServiceModule)
                .build()
        component.inject(testApp)

        scenario = ActivityScenario.launch(CommunityLoginActivity::class.java)
    }

    @Test
    fun `it should finish the login flow when auth is successful`() {
        val passcode = "0123456789"
        val url = URL_PREFIX + passcode.takeLast(4) + URL_SUFFIX
        val identity = "TestUser"
        val requestBody = AuthServiceRequestDTO(passcode, identity)
        val response = AuthServiceResponseDTO("token")
        whenever(authService.getToken(url, requestBody)).thenReturn(Single.just(response))

        enterYourName(identity)
        // TODO Use Espresso for passcode entering as soon as Robolectric bug is fixed https://github.com/robolectric/robolectric/issues/5110
        scenario.onActivity {
            val passcodeEditText = it.findViewById<TextInputEditText>(R.id.community_login_screen_passcode_edittext)
            passcodeEditText.setText(passcode)
        }
        clickLoginButton()

        assertLoadingIndicatorIsDisplayed()

        scheduler.triggerActions()

        assertLoadingIndicatorIsNotDisplayed()

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
