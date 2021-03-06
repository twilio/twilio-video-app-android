package com.twilio.video.app.chat

import kotlinx.coroutines.flow.Flow

interface ChatManager {

    val chatState: Flow<ChatState>
    val chatEvents: Flow<ChatEvent>
    var isUserReadingMessages: Boolean

    fun connect(token: String, chatName: String)
    fun sendMessage(message: String)
    fun disconnect()
}
