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
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.twilio.video.VideoDimensions
import com.twilio.video.Vp8Codec
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.Preferences.ENVIRONMENT
import com.twilio.video.app.data.Preferences.ENVIRONMENT_DEFAULT
import com.twilio.video.app.data.api.dto.Topology
import retrofit2.HttpException
import timber.log.Timber

class InternalTokenService(
    private val sharedPreferences: SharedPreferences,
    private val internalDevTokenApi: InternalTokenApi,
    private val internalStageTokenApi: InternalTokenApi,
    private val internalProdTokenApi: InternalTokenApi
) : TokenService {

    override suspend fun getToken(identity: String?, roomName: String?): String {
        val env = sharedPreferences.getString(ENVIRONMENT, ENVIRONMENT_DEFAULT)
        val authService = resolveAuthService(env!!)
        val requestMsg = if (null != roomName)
            AuthServiceRequestDTO(null, identity, roomName, true)
        else AuthServiceRequestDTO(null, identity)
        try {
            Timber.d("app service env = $authService")
            authService.getToken(requestMsg).let { response ->
                    return handleResponse(response)
                        ?: throw AuthServiceException(message = "Token cannot be null")
                }
        } catch (httpException: HttpException) {
            handleException(httpException)
        }
        return ""
    }

    private fun resolveAuthService(env: String): InternalTokenApi {
        return when (env) {
            TWILIO_API_DEV_ENV -> internalDevTokenApi
            TWILIO_API_STAGE_ENV -> internalStageTokenApi
            else -> internalProdTokenApi
        }
    }

    private fun handleResponse(response: AuthServiceResponseDTO): String? {
        return response.token?.let { token ->
            Timber.d("Response successfully retrieved from the Twilio auth service: %s",
                response)
            response.topology?.let { serverTopology ->
                val isTopologyChange =
                    sharedPreferences.getString(Preferences.TOPOLOGY, Preferences.TOPOLOGY_DEFAULT) != serverTopology.value
                if (isTopologyChange) {
                    val (enableSimulcast, videoDimensionsIndex) = when (serverTopology) {
                        Topology.GROUP, Topology.GROUP_SMALL -> true to Preferences.VIDEO_CAPTURE_RESOLUTION_DEFAULT
                        Topology.PEER_TO_PEER, Topology.GO -> false to Preferences.VIDEO_DIMENSIONS.indexOf(
                            VideoDimensions.HD_720P_VIDEO_DIMENSIONS
                        ).toString()
                    }
                    Timber.d("Server topology has changed to %s. Setting the codec to Vp8 with simulcast set to %s",
                        serverTopology, enableSimulcast)
                    sharedPreferences.edit {
                        putString(Preferences.TOPOLOGY, serverTopology.value)
                        putString(Preferences.VIDEO_CODEC, Vp8Codec.NAME)
                        putBoolean(Preferences.VP8_SIMULCAST, enableSimulcast)
                        putString(Preferences.VIDEO_CAPTURE_RESOLUTION, videoDimensionsIndex)
                        commit()
                    }
                }
            }
            token
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
                            throw AuthServiceException(
                                httpException,
                                AuthServiceError.value(error.message))
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
