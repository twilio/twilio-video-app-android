package com.twilio.video.app.ui.login

import android.content.Intent
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
import com.twilio.video.app.android.SharedPreferencesWrapper
import com.twilio.video.app.auth.CommunityAuthModule
import com.twilio.video.app.auth.CommunityAuthenticator
import com.twilio.video.app.data.AuthServiceModule
import com.twilio.video.app.data.PASSCODE
import com.twilio.video.app.data.Preferences.DISPLAY_NAME
import com.twilio.video.app.data.api.AuthService
import com.twilio.video.app.data.api.AuthServiceRepository
import com.twilio.video.app.data.api.AuthServiceRequestDTO
import com.twilio.video.app.data.api.AuthServiceResponseDTO
import com.twilio.video.app.data.api.URL_PREFIX
import com.twilio.video.app.data.api.URL_SUFFIX
import com.twilio.video.app.screen.assertErrorDialogIsDisplayed
import com.twilio.video.app.screen.assertExpiredPasscodeErrorIsDisplayed
import com.twilio.video.app.screen.assertInvalidPasscodeErrorIsDisplayed
import com.twilio.video.app.screen.assertLoadingIndicatorIsDisplayed
import com.twilio.video.app.screen.assertLoadingIndicatorIsNotDisplayed
import com.twilio.video.app.screen.assertLoginButtonIsDisabled
import com.twilio.video.app.screen.assertLoginButtonIsEnabled
import com.twilio.video.app.screen.assertThatPasscodeErrorIsDisabled
import com.twilio.video.app.screen.clickLoginButton
import com.twilio.video.app.screen.enterYourName
import com.twilio.video.app.security.SecurePreferencesFake
import com.twilio.video.app.security.SecurityModule
import com.twilio.video.app.ui.room.RoomActivity
import com.twilio.video.app.util.EXPIRED_PASSCODE_ERROR
import com.twilio.video.app.util.INVALID_PASSCODE_ERROR
import com.twilio.video.app.util.MainCoroutineScopeRule
import com.twilio.video.app.util.getMockHttpException
import com.twilio.video.app.util.getSharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

private const val IDENTITY = "Identity"
private const val VALID_PASSCODE = "0123456789"

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(application = TestApp::class)
class CommunityLoginActivityTest {

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private val url = URL_PREFIX + VALID_PASSCODE.substring(6) + URL_SUFFIX
    private val requestBody = AuthServiceRequestDTO(VALID_PASSCODE, IDENTITY)
    private lateinit var scenario: ActivityScenario<CommunityLoginActivity>
    private val testApp = ApplicationProvider.getApplicationContext<TestApp>()
    private val authService: AuthService = mock()
    private val preferences = getSharedPreferences(testApp)
    private val securePreferences = SecurePreferencesFake()
    private val authServiceRepository = AuthServiceRepository(authService,
            securePreferences, SharedPreferencesWrapper(preferences))
    private val securityModule: SecurityModule = mock {
        whenever(mock.providesSecurePreferences(any(), any())).thenReturn(securePreferences)
    }
    private val authServiceModule: AuthServiceModule = mock {
        whenever(mock.providesOkHttpClient()).thenReturn(mock())
        whenever(mock.providesAuthService(any())).thenReturn(authService)
        whenever(mock.providesTokenService(any(), any(), any())).thenReturn(authServiceRepository)
    }
    private val authenticator = CommunityAuthenticator(
            preferences,
            securePreferences,
            authServiceRepository,
            coroutineScope.coroutineContext)
    private val communityAuthModule: CommunityAuthModule = mock {
        whenever(mock.providesCommunityAuthenticator(any(), any(), any())).thenReturn(authenticator)
    }

    @Before
    fun setUp() {
        val component = DaggerCommunityIntegrationTestComponent
                .builder()
                .applicationModule(ApplicationModule(testApp))
                .authServiceModule(authServiceModule)
                .communityAuthModule(communityAuthModule)
                .securityModule(securityModule)
                .build()
        component.inject(testApp)

        scenario = ActivityScenario.launch(CommunityLoginActivity::class.java)
    }

    @Test
    fun `it should store the passcode and identity and finish the login flow when auth is successful`() {
        coroutineScope.runBlockingTest {
            val response = AuthServiceResponseDTO("token")
            whenever(authService.getToken(url, requestBody)).thenReturn(response)

            enterYourName(IDENTITY)
            enterPasscode(VALID_PASSCODE)
            clickLoginButton()

            assertThat(securePreferences.getSecureString(PASSCODE), equalTo(VALID_PASSCODE))
            assertThat(preferences.getString(DISPLAY_NAME, null), equalTo(IDENTITY))

            val roomActivityRequest = Shadows.shadowOf(testApp).nextStartedActivity
            assertThat(roomActivityRequest.component, equalTo(Intent(testApp, RoomActivity::class.java).component))
        }
    }

    @Test
    fun `it should display an error message when the auth request fails from an invalid passcode`() {
        coroutineScope.runBlockingTest {
            val response = AuthServiceResponseDTO("token")
            val exception = getMockHttpException(INVALID_PASSCODE_ERROR)
            whenever(authService.getToken(url, requestBody))
                    .thenThrow(exception)
                    .thenReturn(response)

            enterYourName(IDENTITY)
            enterPasscode(VALID_PASSCODE)
            clickLoginButton()

            assertInvalidPasscodeErrorIsDisplayed()
        }
    }

    @Test
    fun `it should display an error message when the auth request fails from an expired passcode`() {
        coroutineScope.runBlockingTest {
            val response = AuthServiceResponseDTO("token")
            val exception = getMockHttpException(EXPIRED_PASSCODE_ERROR)
            whenever(authService.getToken(url, requestBody))
                    .thenThrow(exception)
                    .thenReturn(response)

            enterYourName(IDENTITY)
            enterPasscode(VALID_PASSCODE)
            clickLoginButton()

            assertExpiredPasscodeErrorIsDisplayed()

            // Re enter input again for valid response
            enterYourName(IDENTITY)
            enterPasscode(VALID_PASSCODE)
            clickLoginButton()

            assertThatPasscodeErrorIsDisabled()
        }
    }

    @Test
    fun `it should display an error message when the auth request fails for an unknown reason`() {
        enterYourName(IDENTITY)
        enterPasscode("123")
        clickLoginButton()

        assertErrorDialogIsDisplayed()
    }

    @Test
    fun `it should enable the login button after all required fields have been entered`() {
        assertLoginButtonIsDisabled()

        enterYourName(IDENTITY)

        assertLoginButtonIsDisabled()

        enterPasscode(VALID_PASSCODE)

        assertLoginButtonIsEnabled()
    }

    @Test
    fun `it should enable and disable the proper view state before and after login`() {
        coroutineScope.runBlockingTest {
            val passcode = "0123456789"
            val url = URL_PREFIX + passcode.substring(6) + URL_SUFFIX
            val identity = "TestUser"
            val requestBody = AuthServiceRequestDTO(passcode, identity)
            val response = AuthServiceResponseDTO("token")
            whenever(authService.getToken(url, requestBody)).thenReturn(response)

            enterYourName(identity)
            enterPasscode(VALID_PASSCODE)
            pauseDispatcher()
            clickLoginButton()

            assertLoadingIndicatorIsDisplayed()
            assertLoginButtonIsDisabled()
            resumeDispatcher()

            assertLoadingIndicatorIsNotDisplayed()
            assertLoginButtonIsEnabled()
        }
    }

    // TODO Use Espresso for passcode entering as soon as Robolectric bug is fixed https://github.com/robolectric/robolectric/issues/5110
    private fun enterPasscode(passcode: String) {
        scenario.onActivity {
            val passcodeEditText = it.findViewById<TextInputEditText>(R.id.community_login_screen_passcode_edittext)
            passcodeEditText.setText(passcode)
        }
    }
}
