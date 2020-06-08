package com.twilio.video.app.ui.login

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.video.app.ApplicationModule
import com.twilio.video.app.DaggerIntegrationTestComponent
import com.twilio.video.app.TestApp
import com.twilio.video.app.TestWrapperAuthModule
import com.twilio.video.app.auth.FirebaseWrapper
import com.twilio.video.app.auth.GoogleAuthProviderWrapper
import com.twilio.video.app.auth.GoogleAuthWrapper
import com.twilio.video.app.auth.GoogleSignInOptionsWrapper
import com.twilio.video.app.auth.GoogleSignInWrapper
import com.twilio.video.app.screen.clickGoogleSignInButton
import com.twilio.video.app.ui.room.RoomActivity
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApp::class)
class LoginActivityTest {

    private lateinit var scenario: ActivityScenario<LoginActivity>
    private val testApp = ApplicationProvider.getApplicationContext<TestApp>()
    private val googleSignInActivityRequest = Intent(testApp, TestActivity::class.java)
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
        val component = DaggerIntegrationTestComponent
                .builder()
                .applicationModule(ApplicationModule(testApp))
                .testWrapperAuthModule(TestWrapperAuthModule(firebaseWrapper,
                        googleAuthWrapper,
                        googleSignInWrapper,
                        googleSignInOptionsWrapper,
                        googleAuthProviderWrapper)
                )
                .build()
        component.inject(testApp)
        scenario = ActivityScenario.launch(LoginActivity::class.java)
    }

    @Test
    fun `it should finish the login flow when google login is successful`() {
        val googleSignInActivityResult = Intent()
        val signInAccount: GoogleSignInAccount = mock {
            whenever(mock.idToken).thenReturn("123456")
            whenever(mock.email).thenReturn("test@twilio.com")
        }
        val googleSignInResult: GoogleSignInResult = mock {
            whenever(mock.isSuccess).thenReturn(true)
            whenever(mock.signInAccount).thenReturn(signInAccount)
        }
        whenever(googleAuthWrapper.getSignInResultFromIntent(googleSignInActivityResult)).thenReturn(googleSignInResult)
        val authResult: Task<AuthResult> = mock {
            whenever(mock.isSuccessful).thenReturn(true)
            whenever(mock.addOnCompleteListener(any())).thenAnswer {
                (it.getArgument(0) as OnCompleteListener<AuthResult>).onComplete(mock)
            }
        }
        whenever(googleAuthProviderWrapper.getCredential(any())).thenReturn(mock())
        whenever(firebaseAuth.signInWithCredential(any())).thenReturn(authResult)

        clickGoogleSignInButton()

        val actualActivityRequest = shadowOf(testApp).nextStartedActivity
        assertThat(actualActivityRequest, equalTo(googleSignInActivityRequest))

        scenario.onActivity {
            // Trigger onActivityResult
            shadowOf(it).receiveResult(
                    actualActivityRequest,
                    RESULT_OK,
                    googleSignInActivityResult
            )
        }

        val roomActivityRequest = shadowOf(testApp).nextStartedActivity
        assertThat(roomActivityRequest.component, equalTo(Intent(testApp, RoomActivity::class.java).component))
    }

    class TestActivity : AppCompatActivity()
}
