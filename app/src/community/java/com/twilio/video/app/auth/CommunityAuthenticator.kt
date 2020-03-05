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
import com.twilio.video.app.auth.LoginResult.CommunityLoginSuccessResult
import com.twilio.video.app.data.PASSCODE
import com.twilio.video.app.data.Preferences.DISPLAY_NAME
import com.twilio.video.app.data.api.TokenService
import com.twilio.video.app.util.putString
import com.twilio.video.app.util.remove
import io.reactivex.Observable

class CommunityAuthenticator constructor(
    private val sharedPreferences: SharedPreferences,
    private val tokenService: TokenService
) : Authenticator {

    override fun login(loginEventObservable: Observable<LoginEvent>): Observable<LoginResult> {
        return Observable.empty()
    }

    override fun login(loginEvent: LoginEvent): Observable<LoginResult> {
        return if (loginEvent is LoginEvent.CommunityLoginEvent) {
            sharedPreferences.putString(DISPLAY_NAME, loginEvent.identity)
            sharedPreferences.putString(PASSCODE, loginEvent.passcode) // TODO Encrypt

            tokenService.getToken(loginEvent.identity)
                    .map { CommunityLoginSuccessResult as LoginResult }
                    .doOnError {
                        sharedPreferences.remove(DISPLAY_NAME)
                        sharedPreferences.remove(PASSCODE)
                    }
                    .toObservable()
        } else Observable.empty<LoginResult>()
    }

    override fun loggedIn(): Boolean {
        return !sharedPreferences.getString(PASSCODE, null).isNullOrEmpty()
    }

    override fun logout() {
        sharedPreferences.remove(DISPLAY_NAME)
        sharedPreferences.remove(PASSCODE)
    }
}
