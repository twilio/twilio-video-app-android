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

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.twilio.video.app.R
import com.twilio.video.app.base.BaseActivity
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.databinding.LoginActivityBinding
import com.twilio.video.app.ui.room.RoomActivity
import javax.inject.Inject
import timber.log.Timber

private const val RC_SIGN_IN = 20

class LoginActivity : BaseActivity() {

    private lateinit var authUI: FirebaseAuth
    private lateinit var binding: LoginActivityBinding

    @Inject
    internal lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        authUI = FirebaseAuth.getInstance()
        setContentView(binding.root)
        binding.signInButton.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            navigateToFirebaseUIAuth()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            binding.progressBar.visibility = View.GONE
            if (resultCode == Activity.RESULT_OK) {
                authUI.currentUser?.let { user ->
                    saveIdentity(user)
                    startLobbyActivity()
                }
            } else {
                IdpResponse.fromResultIntent(data)?.let {
                    Timber.e(it.error)
                    showAuthErrorDialog()
                }
            }
        }
    }

    private fun navigateToFirebaseUIAuth() {
        val acceptedDomain = "twilio.com"
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .setHostedDomain(acceptedDomain)
                .build()
        val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().setAllowNewAccounts(false).build(),
                AuthUI.IdpConfig.GoogleBuilder().setSignInOptions(signInOptions).build())
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .setAlwaysShowSignInMethodScreen(true)
                        .setTheme(R.style.GreenTheme)
                        .build(),
                RC_SIGN_IN)
    }

    private fun showAuthErrorDialog() {
        AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                .setTitle(getString(R.string.login_screen_auth_error_title))
                .setMessage(getString(R.string.login_screen_auth_error_desc))
                .setPositiveButton("OK", null)
                .show()
    }

    private fun startLobbyActivity() {
        RoomActivity.startActivity(this, intent.data)
        finish()
    }

    private fun saveIdentity(user: FirebaseUser) {
        sharedPreferences.edit {
            val email = user.email
            putString(Preferences.EMAIL, email)
            putString(Preferences.DISPLAY_NAME, user.displayName ?: email)
        }
    }
}
