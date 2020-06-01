package com.twilio.video.app.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.twilio.video.app.auth.InternalLoginResult.GoogleLoginIntentResult
import com.twilio.video.app.auth.InternalLoginResult.GoogleLoginSuccessResult
import com.twilio.video.app.auth.LoginEvent.GoogleLoginEvent
import com.twilio.video.app.auth.LoginEvent.GoogleLoginIntentRequestEvent
import com.twilio.video.app.util.plus
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

// TODO unit test as part of https://issues.corp.twilio.com/browse/AHOYAPPS-140
class GoogleAuthProvider @JvmOverloads internal constructor(
    private val firebaseWrapper: FirebaseWrapper,
    context: Context,
    private val googleAuthWrapper: GoogleAuthWrapper,
    googleSignInWrapper: GoogleSignInWrapper,
    googleSignInOptionsWrapper: GoogleSignInOptionsWrapper,
    private val googleAuthProviderWrapper: GoogleAuthProviderWrapper,
    private val acceptedDomain: String? = null,
    private val disposables: CompositeDisposable = CompositeDisposable()
) : AuthenticationProvider {

    companion object {
        fun newInstance(
            context: Context,
            googleSignInOptions: GoogleSignInOptions,
            acceptedDomain: String? = null
        ): AuthenticationProvider =
            GoogleAuthProvider(
                    FirebaseWrapper(),
                    context,
                    GoogleAuthWrapper(),
                    GoogleSignInWrapper(),
                    GoogleSignInOptionsWrapper(googleSignInOptions),
                    GoogleAuthProviderWrapper(),
                    acceptedDomain)
    }

    private val googleSignInClient: GoogleSignInClient =
            googleSignInWrapper.getClient(context, googleSignInOptionsWrapper)

    override fun login(loginEventObservable: Observable<LoginEvent>): Observable<LoginResult> {
        return Observable.create { observable ->
            disposables + loginEventObservable.subscribe({ loginEvent ->
                when (loginEvent) {
                    GoogleLoginIntentRequestEvent -> {
                        observable.onNext(GoogleLoginIntentResult(googleSignInClient.signInIntent))
                    }
                    is GoogleLoginEvent -> {
                        getSignInResultFromIntent(loginEvent.signInResultIntent)?.let { result ->
                            if (result.isSuccess) {
                                result.signInAccount?.let { account ->
                                    loginWithAccount(account, observable)
                                } ?: onError(observable)
                            } else {
                                onError(observable)
                            }
                        }
                    }
                }
            },
                    {
                        Timber.e(it)
                    })
        }
    }

    override fun logout() {
        googleSignInClient.signOut()
    }

    private fun loginWithAccount(account: GoogleSignInAccount, observable: ObservableEmitter<LoginResult>) {
        val email = account.email
        val idToken = account.idToken
        if (email.isNullOrBlank()) {
            onError(observable)
            return
        }
        acceptedDomain?.let {
            if (!email.endsWith(it)) {
                onError(observable)
                return
            }
        }
        if (idToken.isNullOrBlank()) {
            onError(observable)
            return
        }
        val credential = googleAuthProviderWrapper.getCredential(idToken)
        firebaseWrapper.instance.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        observable.onNext(GoogleLoginSuccessResult(account))
                        observable.onComplete()
                        disposables.clear()
                    } else {
                        onError(observable)
                    }
                }
    }

    private fun onError(observable: ObservableEmitter<LoginResult>) {
        observable.onError(AuthenticationException())
        disposables.clear()
    }

    private fun getSignInResultFromIntent(data: Intent): GoogleSignInResult? = googleAuthWrapper.getSignInResultFromIntent(data)
}
