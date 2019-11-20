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
import com.google.firebase.auth.FirebaseUser
import com.twilio.video.app.R
import com.twilio.video.app.auth.FirebaseFacade
import com.twilio.video.app.auth.LoginEvent.EmailLogin
import com.twilio.video.app.auth.LoginEvent.GoogleLogin
import com.twilio.video.app.auth.LoginIntentEvent.GoogleSignInIntent
import com.twilio.video.app.base.BaseActivity
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.ui.room.RoomActivity
import com.twilio.video.app.util.plus
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class LoginActivity : BaseActivity(), LoginLandingFragment.Listener, ExistingAccountLoginFragment.Listener {

    @Inject
    internal lateinit var sharedPreferences: SharedPreferences
    @Inject
    internal lateinit var firebaseFacade: FirebaseFacade

    private lateinit var progressDialog: ProgressDialog
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        val user = firebaseFacade.getCurrentUser()
        if (user != null) {
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


    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GOOGLE_SIGN_IN) {
            data?.let {
                disposables + firebaseFacade.login(GoogleLogin(data))
                        .subscribe({
                            processError()
                        }, {
                            Timber.e(it)
                        },
                        {
                            onSignInSuccess()
                        })
            } ?: processError()
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // LoginLandingFragment
    override fun onSignInWithGoogle() {
        val intent = firebaseFacade.getLoginIntent(GoogleSignInIntent)
        showAuthenticatingDialog()
        startActivityForResult(intent, GOOGLE_SIGN_IN)
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
        showAuthenticatingDialog()
        disposables + firebaseFacade.login(EmailLogin(email, password))
                .subscribe({
                    processError()
                }, {
                    Timber.e(it)
                },
                {
                    onSignInSuccess()
                })

    }

    private fun startLobbyActivity() {
        val intent = Intent(this, RoomActivity::class.java)
        startActivity(intent)
        finish()
    }

    // TODO Provide more detailed error handling as part of https://issues.corp.twilio.com/browse/AHOYAPPS-153
    private fun processError() {
        showUnauthorizedEmailDialog()
    }

    private fun showUnauthorizedEmailDialog() {
        AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.login_screen_auth_error_title))
                .setMessage(getString(R.string.login_screen_auth_error_desc))
                .setPositiveButton("OK", null)
                .show()
    }

    private fun dismissAuthenticatingDialog() {
        progressDialog.dismiss()
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
        dismissAuthenticatingDialog()
        startLobbyActivity()
    }
}