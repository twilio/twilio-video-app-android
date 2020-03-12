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

import android.content.SharedPreferences
import com.google.gson.Gson
import com.twilio.video.app.data.PASSCODE
import retrofit2.HttpException
import timber.log.Timber

class AuthServiceRepository(
    private val authService: AuthService,
    private val sharedPreferences: SharedPreferences
) : TokenService {

    override suspend fun getToken(identity: String?, roomName: String?, passcode: String?): String {
        getPasscode(passcode)?.let { passcode ->
            // TODO Use mapper to handle DTOs
            val requestBody = AuthServiceRequestDTO(
                    passcode,
                    identity,
                    roomName)
            val appId = passcode.substring(6)
            val url = URL_PREFIX + appId + URL_SUFFIX

            try {
                val response = authService.getToken(url, requestBody)
                Timber.d("Token returned from Twilio auth service: %s", response)
                return response.token!!
            } catch (e: HttpException) {
                // TODO Handle all error scenarios as part of https://issues.corp.twilio.com/browse/AHOYAPPS-446
                val errorJson = e.response()!!.errorBody()!!.string()
                val errorDTO = Gson().fromJson(errorJson, AuthServiceErrorDTO::class.java)
                Timber.e(e, errorDTO.error!!.explanation)
                val error = AuthServiceError.value(errorDTO.error.message!!)
                throw AuthServiceException(e, error)
            }
        }

        throw IllegalArgumentException("Passcode cannot be null")
    }

    private fun getPasscode(passcode: String?) =
        passcode ?: sharedPreferences.getString(PASSCODE, null)
}