package com.twilio.video.app.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.video.app.auth.LoginResult.GoogleLoginSuccessResult
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class GoogleAuthenticatorTest {
    private val context: Context = mock {
        whenever(mock.getString(any())).thenReturn("")
    }
    private val googleSignInOptionsBuilderWrapper: GoogleSignInOptionsBuilderWrapper = mock {
        whenever(mock.build()).thenReturn(mock())
    }
    private val googleSignInWrapper: GoogleSignInWrapper = mock {
        whenever(mock.getClient(any(), any())).thenReturn(mock())
    }
    private val intent: Intent = mock()
    private val googleSignInAccount: GoogleSignInAccount = mock {
        whenever(mock.email).thenReturn("test@test.com")
        whenever(mock.idToken).thenReturn("123")
        whenever(mock.displayName).thenReturn("test")
    }
    private val googleSignInResult: GoogleSignInResult = mock {
        whenever(mock.isSuccess).thenReturn(true)
        whenever(mock.signInAccount).thenReturn(googleSignInAccount)
    }
    private val googleAuthWrapper = mock<GoogleAuthWrapper> {
        whenever(mock.getSignInResultFromIntent(intent)).thenReturn(googleSignInResult)
    }
    private val authResult: Task<AuthResult> = mock {
        whenever(mock.isSuccessful).thenReturn(true)
        whenever(mock.addOnCompleteListener(any())).thenAnswer {
            (it.getArgument(0) as OnCompleteListener<AuthResult>).onComplete(mock)
        }
    }
    private val googleAuthCredential: AuthCredential = mock()
    private val googleAuthProviderWrapper: GoogleAuthProviderWrapper = mock {
        whenever(mock.getCredential("123")).thenReturn(googleAuthCredential)
    }
    private val firebaseAuth: FirebaseAuth = mock {
        whenever(mock.signInWithCredential(googleAuthCredential)).thenReturn(authResult)
    }
    private val firebaseWrapper: FirebaseWrapper = mock {
        whenever(mock.instance).thenReturn(firebaseAuth)
    }
    private val disposables: CompositeDisposable = mock()

    @Test
    fun `loginWithAccount should login successfully`() {
        val googleAuthenticator = GoogleAuthenticator(
                firebaseWrapper,
                context,
                googleAuthWrapper,
                googleSignInWrapper,
                googleSignInOptionsBuilderWrapper,
                googleAuthProviderWrapper,
                disposables,
                "test.com"
        )
        val testObservable = googleAuthenticator.login(Observable.just(LoginEvent.GoogleLoginEvent(intent))).test()
        testObservable.assertValue(GoogleLoginSuccessResult(googleSignInAccount))
        testObservable.assertComplete()
        verify(disposables).clear()
    }

    @Test
    fun `loginWithAccount should login successfully with no accepted domain`() {
        val googleAuthenticator = GoogleAuthenticator(
                firebaseWrapper,
                context,
                googleAuthWrapper,
                googleSignInWrapper,
                googleSignInOptionsBuilderWrapper,
                googleAuthProviderWrapper,
                disposables
        )
        val testObservable = googleAuthenticator.login(Observable.just(LoginEvent.GoogleLoginEvent(intent))).test()
        testObservable.assertValue(GoogleLoginSuccessResult(googleSignInAccount))
        testObservable.assertComplete()
        verify(disposables).clear()
    }

    @Test
    fun `loginWithAccount should not login successfully when email doesn't match the accepted domain`() {
        whenever(googleSignInAccount.email).thenReturn("test@blah.com")

        val googleAuthenticator = GoogleAuthenticator(
                mock(),
                context,
                googleAuthWrapper,
                googleSignInWrapper,
                googleSignInOptionsBuilderWrapper,
                mock(),
                disposables,
                "test.com"
        )
        val testObservable = googleAuthenticator.login(Observable.just(LoginEvent.GoogleLoginEvent(intent))).test()
        assertThat(testObservable.errorCount(), equalTo(1))
        verify(disposables).clear()
    }

    @Test
    fun `buildGoogleSignInOptions should set hosted domain if accepted domain is not null`() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Test
    fun `buildGoogleSignInOptions should set not hosted domain if accepted domain is null`() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}