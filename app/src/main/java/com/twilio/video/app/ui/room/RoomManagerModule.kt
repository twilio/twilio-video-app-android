package com.twilio.video.app.ui.room

import com.twilio.video.app.ApplicationModule
import com.twilio.video.app.ApplicationScope
import dagger.Module
import dagger.Provides

@Module(includes = [
    ApplicationModule::class])
class RoomManagerModule {

    @Provides
    @ApplicationScope
    fun providesRoomManager(): RoomManager {
        return RoomManager()
    }
}