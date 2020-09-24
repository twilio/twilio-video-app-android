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
import androidx.core.content.edit
import com.twilio.video.app.auth.CommunityLoginResult.CommunityLoginFailureResult
import com.twilio.video.app.auth.CommunityLoginResult.CommunityLoginSuccessResult
import com.twilio.video.app.data.PASSCODE
import com.twilio.video.app.data.Preferences.DISPLAY_NAME
import com.twilio.video.app.data.api.AuthServiceException
import com.twilio.video.app.data.api.TokenService
import com.twilio.video.app.security.SecurePreferences
import io.reactivex.Observable
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.rxSingle
import timber.log.Timber

class CommunityAuthenticator constructor(
    private val sharedPreferences: SharedPreferences,
    private val securePreferences: SecurePreferences,
    private val tokenService: TokenService,
    private val coroutineContext: CoroutineContext = Dispatchers.IO
) : Authenticator {

    override fun login(loginEventObservable: Observable<LoginEvent>): Observable<LoginResult> {
        return Observable.empty()
    }

    override fun login(loginEvent: LoginEvent): Observable<LoginResult> {
        return rxSingle(coroutineContext) {
            if (loginEvent is LoginEvent.CommunityLoginEvent) {
                try {
                    tokenService.getToken(identity = loginEvent.identity, passcode = loginEvent.passcode)

                    sharedPreferences.edit { putString(DISPLAY_NAME, loginEvent.identity) }
                    securePreferences.putSecureString(PASSCODE, loginEvent.passcode)

                    CommunityLoginSuccessResult
                } catch (e: AuthServiceException) {
                    Timber.e(e, "Failed to retrieve token")
                    CommunityLoginFailureResult(e.error) as LoginResult
                }
            } else {
                CommunityLoginFailureResult()
            }
        }.toObservable()
    }

    override fun loggedIn(): Boolean {
        return !securePreferences.getSecureString(PASSCODE).isNullOrEmpty()
    }

    override fun logout() {
        sharedPreferences.edit { remove(DISPLAY_NAME) }
        sharedPreferences.edit { remove(PASSCODE) }
    }
}
