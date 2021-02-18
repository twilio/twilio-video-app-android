package com.twilio.video.app.chat

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class ChatManagerImpl : ChatManager {

    override fun connect(token: String, chatName: String): Flow<ChatEvent> {
        return emptyFlow()
    }
}
