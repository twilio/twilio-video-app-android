package com.twilio.video.app.sdk

import android.app.Application
import android.content.SharedPreferences
import com.twilio.video.app.data.api.TokenService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class VideoSdkModule {

    @Provides
    @Singleton
    fun providesRoomManager(
        application: Application,
        sharedPreferences: SharedPreferences,
        tokenService: TokenService
    ): RoomManager {
        val connectOptionsFactory = ConnectOptionsFactory(application, sharedPreferences, tokenService)
        val videoClient = VideoClient(application, connectOptionsFactory)
        return RoomManager(application, videoClient, sharedPreferences)
    }
}
