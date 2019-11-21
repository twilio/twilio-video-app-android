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
import com.twilio.video.app.data.Preferences
import io.reactivex.Maybe
import io.reactivex.subjects.SingleSubject

// TODO Remove as part of https://issues.corp.twilio.com/browse/AHOYAPPS-93
class CommunityAuthenticator(private val preferences: SharedPreferences) : Authenticator {

    override fun login(loginEventSubject: SingleSubject<LoginEvent>): Maybe<Authenticator.LoginError> {
        return Maybe.create {
            loginEventSubject.subscribe { tokenLogin ->
                require(tokenLogin is LoginEvent.TokenLogin) { "Expecting ${LoginEvent.TokenLogin::class.simpleName} as LoginEvent" }
                preferences.edit().putString(Preferences.DISPLAY_NAME, tokenLogin.identity).apply()
            }
        }
    }

    override fun loggedIn(): Boolean {
        return !preferences.getString(Preferences.DISPLAY_NAME, null).isNullOrEmpty()
    }

    override fun logout() {
        preferences.edit().remove(Preferences.DISPLAY_NAME).apply()
    }
}
