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

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.twilio.video.app.data.PASSCODE
import com.twilio.video.app.security.SecurePreferences
import retrofit2.HttpException
import timber.log.Timber

class AuthServiceRepository(
    private val authService: AuthService,
    private val securePreferences: SecurePreferences
) : TokenService {

    override suspend fun getToken(identity: String?, roomName: String?): String {
        return getToken(identity, roomName, passcode = null)
    }

    override suspend fun getToken(identity: String?, roomName: String?, passcode: String?): String {
        getPasscode(passcode)?.let { passcode ->
            val requestBody = AuthServiceRequestDTO(
                    passcode,
                    identity,
                    roomName)
            val appId = passcode.substring(6)
            val url = URL_PREFIX + appId + URL_SUFFIX

            try {
                authService.getToken(url, requestBody).let { response ->
                    response.token?.let { token ->
                        Timber.d("Token returned from Twilio auth service: %s", response)
                        return token
                    }
                    throw AuthServiceException(message = "Token cannot be null")
                }
            } catch (httpException: HttpException) {
                handleException(httpException)
            }
        }

        throw IllegalArgumentException("Passcode cannot be null")
    }

    private fun getPasscode(passcode: String?) =
        passcode ?: securePreferences.getSecureString(PASSCODE)

    private fun handleException(httpException: HttpException) {
        Timber.e(httpException)
        httpException.response()?.let { response ->
            response.errorBody()?.let { errorBody ->
                try {
                    val errorJson = errorBody.string()
                    Gson().fromJson(errorJson, AuthServiceErrorDTO::class.java)?.let { errorDTO ->
                        errorDTO.error?.let { error ->
                            val error = AuthServiceError.value(error.message)
                            throw AuthServiceException(httpException, error)
                        }
                    }
                } catch (jsonSyntaxException: JsonSyntaxException) {
                    throw AuthServiceException(jsonSyntaxException)
                }
            }
        }
        throw AuthServiceException(httpException)
    }
}
