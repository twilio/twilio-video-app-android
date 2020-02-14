package com.twilio.video.app.ui.room

import com.twilio.video.app.ApplicationModule
import com.twilio.video.app.ApplicationScope
import com.twilio.video.app.data.DataModule
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
    fun providesRoomManager(): RoomManager {
        return RoomManager()
    }
}