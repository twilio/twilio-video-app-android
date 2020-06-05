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

import com.twilio.video.app.auth.InternalLoginResult.EmailLoginSuccessResult
import com.twilio.video.app.auth.LoginEvent.EmailLoginEvent
import com.twilio.video.app.util.plus
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

// TODO unit test as part of https://issues.corp.twilio.com/browse/AHOYAPPS-140
class EmailAuthProvider @JvmOverloads constructor(
    private val firebaseWrapper: FirebaseWrapper,
    private val disposables: CompositeDisposable = CompositeDisposable()
) : AuthenticationProvider {
    override fun logout() {
        firebaseWrapper.instance.signOut()
    }

    override fun login(loginEventObservable: Observable<LoginEvent>): Observable<LoginResult> {
        return Observable.create { observable ->
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
                                        observable.onNext(EmailLoginSuccessResult(email))
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

    private fun onError(observable: ObservableEmitter<LoginResult>) {
        observable.onError(AuthenticationException())
        disposables.clear()
    }
}
