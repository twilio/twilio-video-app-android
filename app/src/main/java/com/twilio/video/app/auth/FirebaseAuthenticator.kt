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

package com.twilio.video.app.auth

import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.twilio.video.app.ui.login.LoginActivity
import com.twilio.video.app.util.AuthHelper

class FirebaseAuthenticator(private val firebaseWrapper: FirebaseWrapper) : Authenticator {
    override val loginActivity = LoginActivity::class.java

    override fun loggedIn(): Boolean {
        return firebaseWrapper.instance.currentUser != null
    }

    override fun logout() {
        firebaseWrapper.instance.signOut()
    }

    fun login(
            email: String,
            password: String,
            activity: FragmentActivity,
            errorListener: AuthHelper.ErrorListener) {
        require(!(email == null || email.length == 0)) { "Email can't be empty" }
        require(!(password == null || password.length == 0)) { "Password can't be empty" }
        val mAuth = firebaseWrapper.instance
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        activity
                ) { task ->
                    if (!task.isSuccessful) {
                        errorListener.onError(AuthHelper.ERROR_AUTHENTICATION_FAILED)
                    }
                }
    }

    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener ) {
        firebaseWrapper.instance.addAuthStateListener(listener)
    }

    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        firebaseWrapper.instance.removeAuthStateListener(listener)
    }
}
