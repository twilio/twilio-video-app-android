package com.twilio.video.app.sdk

import android.app.Application
import android.content.SharedPreferences
import com.twilio.video.app.ApplicationModule
import com.twilio.video.app.ApplicationScope
import com.twilio.video.app.data.DataModule
import com.twilio.video.app.data.api.TokenService
import com.twilio.video.app.data.api.VideoAppServiceModule
import dagger.Module
import dagger.Provides

@Module(includes = [
    ApplicationModule::class,
    DataModule::class,
    VideoAppServiceModule::class])
class VideoSdkModule {

    @Provides
    fun providesConnectOptionsFactory(
        application: Application,
        sharedPreferences: SharedPreferences,
        tokenService: TokenService
    ): ConnectOptionsFactory =
            ConnectOptionsFactory(application, sharedPreferences, tokenService)

    @Provides
    fun providesRoomFactory(
        application: Application,
        connectOptionsFactory: ConnectOptionsFactory
    ): VideoClient =
            VideoClient(application, connectOptionsFactory)

    @Provides
    @ApplicationScope
    fun providesRoomManager(
        application: Application,
        videoClient: VideoClient
    ): RoomManager =
            RoomManager(application, videoClient)
}
