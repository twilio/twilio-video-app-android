package com.twilio.video.app.ui.room

import android.app.Application
import android.content.SharedPreferences
import com.twilio.video.app.ApplicationModule
import com.twilio.video.app.ApplicationScope
import com.twilio.video.app.data.DataModule
import com.twilio.video.app.data.api.TokenService
import com.twilio.video.app.data.api.VideoAppServiceModule
import com.twilio.video.app.sdk.RoomManager
import dagger.Module
import dagger.Provides

@Module(includes = [
    ApplicationModule::class,
    DataModule::class,
    VideoAppServiceModule::class])
class RoomManagerModule {

    @Provides
    @ApplicationScope
    fun providesRoomManager(
        application: Application,
        sharedPreferences: SharedPreferences,
        tokenService: TokenService
    ): RoomManager {
        return RoomManager(application, sharedPreferences, tokenService)
    }
}