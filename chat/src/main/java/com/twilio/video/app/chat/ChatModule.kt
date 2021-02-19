package com.twilio.video.app.chat

import android.app.Application
import com.twilio.video.app.core.ApplicationModule
import dagger.Module
import dagger.Provides

@Module(includes = [ ApplicationModule::class ])
class ChatModule {

    @Provides
    fun providesChatManager(application: Application): ChatManager = ChatManagerImpl(application)
}
