package com.twilio.video.app.chat

import dagger.Module
import dagger.Provides

@Module
class ChatModule {

    @Provides
    fun providesChatManager(): ChatManager = ChatManagerImpl()
}
