package com.twilio.video.app.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GoogleAuthenticatorTest {

    private val context : Context = ApplicationProvider.getApplicationContext()
    private val firebaseWrapper = mock<FirebaseWrapper>()
    private val firebaseAuth = mock<FirebaseAuth>()
    private val googleAuthWrapper = mock<GoogleAuthWrapper>()
    private val googleSignInWrapper = mock<GoogleSignInWrapper>()
    private val googleSignInOptionsWrapper = mock<GoogleSignInOptionsBuilderWrapper>()
    private val googleSignInOptions = mock<GoogleSignInOptions>()
    private val googleSignInClient = mock<GoogleSignInClient>()
    private lateinit var googleAuthenticator: GoogleAuthenticator

    @Before
    fun setUp() {
        whenever(googleSignInOptionsWrapper.build()).thenReturn(googleSignInOptions)
        whenever(googleSignInWrapper.getClient(context, googleSignInOptions)).thenReturn(googleSignInClient)
        whenever(firebaseWrapper.instance).thenReturn(firebaseAuth)
        googleAuthenticator = GoogleAuthenticator(
                firebaseWrapper,
                context,
                googleAuthWrapper,
                googleSignInWrapper,
                googleSignInOptionsWrapper)
    }

    @Test
    fun `loggedIn should return true when user is logged in`() {
        whenever(firebaseAuth.currentUser).thenReturn(mock())

        assertThat(googleAuthenticator.loggedIn(), equalTo(true))
    }

    @Test
    fun `loggedIn should return false when user is not logged in`() {
        assertThat(googleAuthenticator.loggedIn(), equalTo(false))
    }

    @Ignore
    @Test
    fun `logout should emit success when logout is successful`() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Ignore
    @Test
    fun `logout should emit error when logout is unsuccessful`() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Ignore
    @Test
    fun `login should emit success when login is successful`() {
//        val account = mock<GoogleSignInAccount>{
//            whenever(mock.email).thenReturn("test@twilio.com")
//            whenever(mock.idToken).thenReturn("IdToken")
//        }
//        val authCredential = mock<AuthCredential>()
//        whenever(googleAuthProviderWrapper.getCredential("IdToken")).thenReturn(authCredential)
//        val task = mock<Task<AuthResult>>()
//        whenever(firebaseAuth.signInWithCredential(authCredential)).thenReturn(task)
//        val argumentCaptor = argumentCaptor<OnCompleteListener<AuthResult>>()
//
//        val testObserver = googleAuthenticator.login(account).test()
//
//        verify(task).addOnCompleteListener(argumentCaptor.capture())
//        testObserver.assertComplete()
    }

    @Ignore
    @Test
    fun `login should emit error when login is unsuccessful`() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Ignore
    @Test
    fun `login should emit error when email doesn't contain a twilio domain`() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
