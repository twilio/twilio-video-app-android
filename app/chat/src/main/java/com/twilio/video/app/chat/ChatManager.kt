package com.twilio.video.app.chat

import kotlinx.coroutines.flow.Flow

interface ChatManager {

    fun connect(token: String, chatName: String): Flow<ChatEvent>
}
