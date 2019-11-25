package com.twilio.video.app.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.twilio.video.app.R
import com.twilio.video.app.auth.LoginEvent.GoogleLoginEvent
import com.twilio.video.app.auth.LoginEvent.GoogleLoginIntentRequestEvent
import com.twilio.video.app.auth.LoginResult.GoogleLoginIntentResult
import com.twilio.video.app.auth.LoginResult.GoogleLoginSuccessResult
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.util.plus
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

// TODO unit test as part of https://issues.corp.twilio.com/browse/AHOYAPPS-140
class GoogleAuthenticator @JvmOverloads constructor(
    private val firebaseWrapper: FirebaseWrapper,
    context: Context,
    private val googleAuthWrapper: GoogleAuthWrapper,
    private val googleSignInWrapper: GoogleSignInWrapper,
    private val googleSignInOptionsBuilderWrapper: GoogleSignInOptionsBuilderWrapper,
    private val googleAuthProviderWrapper: GoogleAuthProviderWrapper,
    private val sharedPreferences: SharedPreferences,
    private val disposables: CompositeDisposable = CompositeDisposable()
) : AuthenticationProvider {

    private val googleSignInClient: GoogleSignInClient = buildGoogleSignInClient(context)

    override fun login(loginEventObservable: Observable<LoginEvent>): Observable<LoginResult> {
        return Observable.create<LoginResult> { observable ->
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
        if (!email.endsWith("@twilio.com")) {
            onError(observable)
            return
        }
        if (idToken.isNullOrBlank()) {
            onError(observable)
            return
        }
        val credential = googleAuthProviderWrapper.getCredential(idToken)
        firebaseWrapper.instance.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        saveIdentity(account.displayName, email)
                        observable.onNext(GoogleLoginSuccessResult)
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

    private fun buildGoogleSignInClient(context: Context) =
            googleSignInWrapper.getClient(context, buildGoogleSignInOptions(context))

    private fun buildGoogleSignInOptions(context: Context): GoogleSignInOptions {
        return googleSignInOptionsBuilderWrapper.run {
            requestIdToken(context.getString(R.string.default_web_client_id))
            requestEmail()
            setHostedDomain("twilio.com")
            build()
        }
    }

    // TODO Create Facade for SharedPreferences for ease of use and testability
    private fun saveIdentity(displayName: String?, email: String) {
        sharedPreferences
                .edit()
                .putString(Preferences.EMAIL, email)
                .putString(Preferences.DISPLAY_NAME, displayName ?: email)
                .apply()
    }
}
