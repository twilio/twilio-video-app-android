package com.twilio.video.app.ui.login

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.video.app.*
import com.twilio.video.app.auth.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApp::class)
class LoginActivityTest : IntegrationTest {


    private val firebaseAuth: FirebaseAuth= mock()
    private val firebaseWrapper: FirebaseWrapper = mock {
        whenever(mock.instance).thenReturn(firebaseAuth)
    }
    private val googleAuthWrapper: GoogleAuthWrapper = mock()
    private val googleSignInClient: GoogleSignInClient = mock()
    private val googleSignInWrapper: GoogleSignInWrapper = mock {
        whenever(mock.getClient(any(), any())).thenReturn(googleSignInClient)
    }
    private val googleSignInOptionsBuilderWrapper: GoogleSignInOptionsBuilderWrapper = mock {
        whenever(mock.build()).thenReturn(mock())
    }
    private val googleAuthProviderWrapper: GoogleAuthProviderWrapper = mock()

    @Before
    fun setUp() {
        val testApp = ApplicationProvider.getApplicationContext<TestApp>()
        val component = DaggerIntegrationTestComponent
                .builder()
                .applicationModule(ApplicationModule(testApp))
                .testWrapperAuthModule(TestWrapperAuthModule(firebaseWrapper,
                        googleAuthWrapper,
                        googleSignInWrapper,
                        googleSignInOptionsBuilderWrapper,
                        googleAuthProviderWrapper)
                )
                .build()
        component.inject(testApp)
        ActivityScenario.launch(LoginActivity::class.java)
    }

    @Test
    fun `it should successfully login with Google and navigate to the lobby screen`() {
    }
}