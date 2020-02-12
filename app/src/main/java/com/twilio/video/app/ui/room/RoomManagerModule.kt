package com.twilio.video.app.ui.room

import android.app.Application
import android.content.SharedPreferences
import com.twilio.video.app.ApplicationModule
import com.twilio.video.app.ApplicationScope
import com.twilio.video.app.data.DataModule
import com.twilio.video.app.data.api.VideoAppServiceDelegate
import com.twilio.video.app.data.api.VideoAppServiceModule
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
        sharedPreferences: SharedPreferences,
        videoAppServiceDelegate: VideoAppServiceDelegate,
        application: Application
    ): RoomManager {
        return RoomManager(
                sharedPreferences,
                videoAppServiceDelegate,
                application.applicationContext)
    }
}