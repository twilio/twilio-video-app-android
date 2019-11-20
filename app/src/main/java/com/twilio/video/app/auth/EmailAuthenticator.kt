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

import com.twilio.video.app.auth.Authenticator.MissingEmailError
import com.twilio.video.app.auth.LoginEvent.EmailLogin
import io.reactivex.Maybe

// TODO unit test as part of https://issues.corp.twilio.com/browse/AHOYAPPS-140
class EmailAuthenticator(private val firebaseWrapper: FirebaseWrapper) : Authenticator {

    override fun logout() {
        firebaseWrapper.instance.signOut()
    }

    override fun login(emailLogin: LoginEvent): Maybe<Authenticator.LoginError> {
        require(emailLogin is EmailLogin) { "Expecting ${EmailLogin::class.simpleName} as LoginEvent" }
        return Maybe.create { maybe ->
            emailLogin.run {
                if (email.isBlank()) {
                    maybe.onSuccess(MissingEmailError)
                }
                if (password.isBlank()) {
                    maybe.onSuccess(MissingPasswordError)
                }
                firebaseWrapper.instance.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                maybe.onComplete()
                            } else {
                                maybe.onSuccess(FirebaseLoginError)
                            }
                        }
            }
        }
    }
}

object FirebaseLoginError : Authenticator.LoginError()
object MissingPasswordError : Authenticator.LoginError()
