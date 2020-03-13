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
import com.twilio.video.app.ApplicationScope
import com.twilio.video.app.data.AuthServiceModule
import com.twilio.video.app.data.api.TokenService
import com.twilio.video.app.security.SecurePreferences
import com.twilio.video.app.security.SecurityModule
import dagger.Module
import dagger.Provides

@Module(includes = [
    AuthServiceModule::class,
    SecurityModule::class
])
class CommunityAuthModule {
    @Provides
    @ApplicationScope
    fun providesCommunityAuthenticator(
        preferences: SharedPreferences,
        securePreferences: SecurePreferences,
        tokenService: TokenService
    ): Authenticator {
        return CommunityAuthenticator(preferences, securePreferences, tokenService)
    }
}