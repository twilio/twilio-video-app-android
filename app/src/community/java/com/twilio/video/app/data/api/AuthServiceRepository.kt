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
package com.twilio.video.app.data.api

import io.reactivex.Single
import timber.log.Timber

class AuthServiceRepository(private val authService: AuthService) : TokenService {

    override fun getToken(tokenServiceParameters: TokenServiceParameters): Single<String> {
        val params = tokenServiceParameters as AuthServiceParameters
        val requestBody = AuthServiceRequestDTO(
                params.passcode,
                params.userIdentity,
                params.roomName)
        val appId = params.passcode.takeLast(4)
        val url = "https://video-app-$appId-dev.twil.io/token"

        return authService.getToken(url, requestBody)
                .doOnSuccess { Timber.d("Token returned from Twilio auth service: %s", it.token) }
                .map { it.token }
    }
}