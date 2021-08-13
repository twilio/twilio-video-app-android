package com.twilio.video.app.sdk

import android.app.Application
import android.content.SharedPreferences
import com.twilio.video.app.ApplicationScope
import com.twilio.video.app.data.api.TokenService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
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
        videoClient: VideoClient,
        sharedPreferences: SharedPreferences
    ): RoomManager =
            RoomManager(application, videoClient, sharedPreferences)
}
