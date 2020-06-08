/*
 * Copyright (C) 2019 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video.app.ui.login

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import butterknife.ButterKnife
import com.twilio.video.app.R
import com.twilio.video.app.auth.Authenticator
import com.twilio.video.app.auth.InternalLoginResult.EmailLoginSuccessResult
import com.twilio.video.app.auth.InternalLoginResult.GoogleLoginIntentResult
import com.twilio.video.app.auth.InternalLoginResult.GoogleLoginSuccessResult
import com.twilio.video.app.auth.LoginEvent
import com.twilio.video.app.auth.LoginEvent.GoogleLoginEvent
import com.twilio.video.app.auth.LoginEvent.GoogleLoginIntentRequestEvent
import com.twilio.video.app.base.BaseActivity
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.ui.room.RoomActivity
import com.twilio.video.app.util.plus
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import timber.log.Timber

internal const val GOOGLE_SIGN_IN = 4615

class LoginActivity : BaseActivity(), LoginLandingFragment.Listener, ExistingAccountLoginFragment.Listener {

    @Inject
    internal lateinit var sharedPreferences: SharedPreferences
    @Inject
    internal lateinit var authenticator: Authenticator

    private lateinit var progressDialog: ProgressDialog
    private val disposables = CompositeDisposable()
    private val loginEventSubject: PublishSubject<LoginEvent> = PublishSubject.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        if (authenticator.loggedIn()) {
            onSignInSuccess()
        }
        ButterKnife.bind(this)
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .add(R.id.login_fragment_container, LoginLandingFragment.newInstance())
                    .commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GOOGLE_SIGN_IN) {
            data?.let {
                loginEventSubject.onNext(GoogleLoginEvent(data))
            } ?: processError()
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // LoginLandingFragment
    override fun onSignInWithGoogle() {
        disposables + authenticator
                .login(loginEventSubject.hide())
                .subscribe(
                        {
                            when (it) {
                                is GoogleLoginIntentResult -> {
                                    startActivityForResult(it.intent, GOOGLE_SIGN_IN)
                                }
                                is GoogleLoginSuccessResult -> {
                                    saveIdentity(it.googleSignInAccount.email!!, it.googleSignInAccount.displayName)
                                    onSignInSuccess()
                                }
                            }
                        },
                        {
                            Timber.e(it)
                            processError()
                        })
        loginEventSubject.onNext(GoogleLoginIntentRequestEvent)
        showAuthenticatingDialog()
    }

    // LoginLandingFragment
    override fun onSignInWithEmail() {
        supportFragmentManager
                .beginTransaction()
                .add(R.id.login_fragment_container, ExistingAccountLoginFragment.newInstance())
                .addToBackStack(null)
                .commit()
    }

    // ExistingAccountLoginFragment
    override fun onExistingAccountCredentials(email: String, password: String) {
        disposables + authenticator
                .login(loginEventSubject.hide())
                .subscribe({
                    if (it is EmailLoginSuccessResult) {
                        saveIdentity(email)
                        onSignInSuccess()
                    }
                },
                {
                    Timber.e(it)
                    processError()
                })
        loginEventSubject.onNext(LoginEvent.EmailLoginEvent(email, password))
        showAuthenticatingDialog()
    }

    private fun startLobbyActivity() {
        RoomActivity.startActivity(this, intent.data)
        finish()
    }

    // TODO Provide more detailed error handling as part of https://issues.corp.twilio.com/browse/AHOYAPPS-153
    private fun processError() {
        progressDialog.dismiss()
        showUnauthorizedEmailDialog()
    }

    private fun showUnauthorizedEmailDialog() {
        AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.login_screen_auth_error_title))
                .setMessage(getString(R.string.login_screen_auth_error_desc))
                .setPositiveButton("OK", null)
                .show()
    }

    internal fun showAuthenticatingDialog() {
        progressDialog = ProgressDialog(this, R.style.Authenticating)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setMessage("Authenticating")
        progressDialog.setCancelable(true)
        progressDialog.isIndeterminate = true
        progressDialog.show()
    }

    private fun onSignInSuccess() {
        disposables.clear()
        progressDialog.dismiss()
        startLobbyActivity()
    }

    private fun saveIdentity(email: String, displayName: String? = null) {
        sharedPreferences
                .edit()
                .putString(Preferences.EMAIL, email)
                .putString(Preferences.DISPLAY_NAME, displayName ?: email)
                .apply()
    }
}
