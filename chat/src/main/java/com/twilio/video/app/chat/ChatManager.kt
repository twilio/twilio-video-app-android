package com.twilio.video.app.chat

import kotlinx.coroutines.flow.Flow

interface ChatManager {

    val chatState: Flow<ChatState>
    val chatEvents: Flow<ChatEvent>

    fun connect(token: String, chatName: String)
}
