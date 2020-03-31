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
import com.twilio.video.app.data.Preferences.ENVIRONMENT
import com.twilio.video.app.data.Preferences.ENVIRONMENT_DEFAULT
import com.twilio.video.app.data.Preferences.RECORD_PARTICIPANTS_ON_CONNECT
import com.twilio.video.app.data.Preferences.RECORD_PARTICIPANTS_ON_CONNECT_DEFAULT
import com.twilio.video.app.data.Preferences.TOPOLOGY
import com.twilio.video.app.data.Preferences.TOPOLOGY_DEFAULT
import timber.log.Timber

class VideoAppServiceDelegate(
    private val sharedPreferences: SharedPreferences,
    private val videoAppServiceDev: VideoAppService,
    private val videoAppServiceStage: VideoAppService,
    private val videoAppServiceProd: VideoAppService
) : TokenService {

    override suspend fun getToken(identity: String?, roomName: String?): String {
        val topology = sharedPreferences.getString(
                TOPOLOGY,
                TOPOLOGY_DEFAULT)
        val isRecordParticipantsOnConnect = sharedPreferences.getBoolean(
                RECORD_PARTICIPANTS_ON_CONNECT,
                RECORD_PARTICIPANTS_ON_CONNECT_DEFAULT)
        val env = sharedPreferences.getString(
                ENVIRONMENT, ENVIRONMENT_DEFAULT)

        val videoAppService = resolveVideoAppService(env!!)
        Timber.d("app service env = $videoAppService")
        return videoAppService.getToken(
                identity,
                roomName,
                "production",
                topology,
                isRecordParticipantsOnConnect)
    }

    private fun resolveVideoAppService(env: String): VideoAppService {
        return when (env) {
            TWILIO_API_DEV_ENV -> videoAppServiceDev
            TWILIO_API_STAGE_ENV -> videoAppServiceStage
            else -> videoAppServiceProd
        }
    }
}
