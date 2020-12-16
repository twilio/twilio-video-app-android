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
import com.twilio.video.VideoDimensions.HD_720P_VIDEO_DIMENSIONS
import com.twilio.video.Vp8Codec
import com.twilio.video.app.android.SharedPreferencesWrapper
import com.twilio.video.app.data.PASSCODE
import com.twilio.video.app.data.Preferences.TOPOLOGY
import com.twilio.video.app.data.Preferences.VIDEO_CAPTURE_RESOLUTION
import com.twilio.video.app.data.Preferences.VIDEO_CAPTURE_RESOLUTION_DEFAULT
import com.twilio.video.app.data.Preferences.VIDEO_CODEC
import com.twilio.video.app.data.Preferences.VIDEO_DIMENSIONS
import com.twilio.video.app.data.Preferences.VP8_SIMULCAST
import com.twilio.video.app.data.api.model.Topology.GO
import com.twilio.video.app.data.api.model.Topology.GROUP
import com.twilio.video.app.data.api.model.Topology.GROUP_SMALL
import com.twilio.video.app.data.api.model.Topology.PEER_TO_PEER
import com.twilio.video.app.security.SecurePreferences
import retrofit2.HttpException
import timber.log.Timber

private const val LEGACY_PASSCODE_SIZE = 10
private const val PASSCODE_SIZE = 14

class AuthServiceRepository(
    private val authService: AuthService,
    private val securePreferences: SecurePreferences,
    private val sharedPreferences: SharedPreferencesWrapper
) : TokenService {
    override suspend fun getToken(identity: String?, roomName: String?): String {
        return getToken(identity, roomName, passcode = null)
    }

    override suspend fun getToken(identity: String?, roomName: String?, passcode: String?): String {
        getPasscode(passcode)?.let { passcode ->
            val (requestBody, url) = buildRequest(passcode, identity, roomName)

            try {
                authService.getToken(url, requestBody).let { response ->
                    return handleResponse(response)
                            ?: throw AuthServiceException(message = "Token cannot be null")
                }
            } catch (httpException: HttpException) {
                handleException(httpException)
            }
        }

        throw IllegalArgumentException("Passcode cannot be null")
    }

    private fun buildRequest(
        passcode: String,
        identity: String?,
        roomName: String?
    ): Pair<AuthServiceRequestDTO, String> {
        val requestBody = AuthServiceRequestDTO(
                passcode,
                identity,
                roomName)
        val appId = passcode.substring(6, 10)
        val serverlessId = passcode.substring(10)
        val url = if (passcode.length == PASSCODE_SIZE) {
            "$URL_PREFIX$appId-$serverlessId$URL_SUFFIX"
        } else {
            "$URL_PREFIX$appId$URL_SUFFIX"
        }
        return Pair(requestBody, url)
    }

    private fun handleResponse(response: AuthServiceResponseDTO): String? {
        return response.token?.let { token ->
            Timber.d("Response successfully retrieved from the Twilio auth service: %s",
                    response)
            response.topology?.let { serverTopology ->
                val isTopologyChange =
                        sharedPreferences.getString(TOPOLOGY, null) != serverTopology.value
                if (isTopologyChange) {
                    sharedPreferences.edit { putString(TOPOLOGY, serverTopology.value) }
                    val (enableSimulcast, videoDimensionsIndex) = when (serverTopology) {
                        GROUP, GROUP_SMALL -> true to VIDEO_CAPTURE_RESOLUTION_DEFAULT
                        PEER_TO_PEER, GO -> false to VIDEO_DIMENSIONS.indexOf(HD_720P_VIDEO_DIMENSIONS).toString()
                    }
                    Timber.d("Server topology has changed to %s. Setting the codec to Vp8 with simulcast set to %s",
                            serverTopology, enableSimulcast)
                    sharedPreferences.edit { putString(VIDEO_CODEC, Vp8Codec.NAME) }
                    sharedPreferences.edit { putBoolean(VP8_SIMULCAST, enableSimulcast) }
                    sharedPreferences.edit { putString(VIDEO_CAPTURE_RESOLUTION, videoDimensionsIndex) }
                }
            }
            token
        }
    }

    private fun getPasscode(passcode: String?): String? {
        validatePasscode(passcode)
        return passcode ?: securePreferences.getSecureString(PASSCODE)
    }

    private fun validatePasscode(passcode: String?) {
        passcode?.let { passcode ->
            require(passcode.isNotEmpty() &&
                    (passcode.length == LEGACY_PASSCODE_SIZE ||
                            passcode.length == PASSCODE_SIZE))
        }
    }

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
