package com.twilio.video.app.chat

import com.twilio.video.app.chat.ConnectionState.Disconnected

data class ChatState(
    val connectionState: ConnectionState = Disconnected,
    val messages: List<ChatMessage> = emptyList(),
    val hasUnreadMessages: Boolean = false,
    val isUserReadingMessages: Boolean = false
)

sealed class ConnectionState {
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    object Disconnected : ConnectionState()
}
