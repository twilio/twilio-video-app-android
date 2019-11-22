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

import android.content.SharedPreferences
import com.twilio.video.app.auth.LoginEvent.EmailLoginEvent
import com.twilio.video.app.auth.LoginResult.EmailLoginSuccessResult
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.util.plus
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

// TODO unit test as part of https://issues.corp.twilio.com/browse/AHOYAPPS-140
class EmailAuthenticator @JvmOverloads constructor(
    private val firebaseWrapper: FirebaseWrapper,
    private val sharedPreferences: SharedPreferences,
    private val disposables: CompositeDisposable = CompositeDisposable()
) : Authenticator {
    override fun logout() {
        firebaseWrapper.instance.signOut()
    }

    override fun loggedIn() = firebaseWrapper.instance.currentUser != null

    override fun login(loginEventObservable: Observable<LoginEvent>): Observable<LoginResult> {
        return Observable.create<LoginResult> { observable ->
            disposables + loginEventObservable.subscribe({ loginEvent ->
                if (loginEvent is EmailLoginEvent) {
                    loginEvent.run {
                        if (email.isBlank()) {
                            onError(observable)
                        }
                        if (password.isBlank()) {
                            onError(observable)
                        }
                        firebaseWrapper.instance.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        saveIdentity(task.result?.user?.displayName, email)
                                        observable.onNext(EmailLoginSuccessResult)
                                        observable.onComplete()
                                        disposables.clear()
                                    } else {
                                        onError(observable)
                                    }
                                }
                    }
                }
            }, {
                Timber.e(it)
            })
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

    private fun onError(observable: ObservableEmitter<LoginResult>) {
        observable.onError(AuthenticationException())
        disposables.clear()
    }
}