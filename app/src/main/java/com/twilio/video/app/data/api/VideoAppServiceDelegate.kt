package com.twilio.video.app.data.api

import android.content.SharedPreferences
import com.twilio.video.app.BuildConfig
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.data.api.model.RoomProperties
import io.reactivex.Single
import timber.log.Timber

class VideoAppServiceDelegate(
        private val sharedPreferences: SharedPreferences,
        private val videoAppServiceDev: VideoAppService,
        private val videoAppServiceStage: VideoAppService,
        private val videoAppServiceProd: VideoAppService) : TokenService {

    override fun getToken(identity: String, roomProperties: RoomProperties): Single<String> {
        val env = sharedPreferences.getString(
                Preferences.ENVIRONMENT, Preferences.ENVIRONMENT_DEFAULT)

        val appEnv = resolveAppEnvironment(BuildConfig.FLAVOR)
        val videoAppService = resolveVideoAppService(env!!)
        Timber.d("app environment = $appEnv, app service env = $videoAppService")
        return videoAppService.getToken(
                identity,
                roomProperties.name,
                appEnv,
                roomProperties.topology.string,
                roomProperties.isRecordParticipantsOnConnect)
    }

    private fun resolveAppEnvironment(appFlavor: String): String {
        // Video App Service only accepts internal and production for app environment
        return if(TWILIO_API_DEV_ENV == appFlavor) {
            "internal"
        } else "production"
    }

    private fun resolveVideoAppService(env: String): VideoAppService {
        return when (env) {
            TWILIO_API_DEV_ENV -> videoAppServiceDev
            TWILIO_API_STAGE_ENV -> videoAppServiceStage
            else -> videoAppServiceProd
        }
    }
}
