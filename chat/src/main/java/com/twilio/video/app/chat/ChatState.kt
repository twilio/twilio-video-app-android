package com.twilio.video.app.chat

import com.twilio.video.app.chat.ConnectionState.Disconnected

data class ChatState(
    val connectionState: ConnectionState = Disconnected
)

sealed class ConnectionState {
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    object Disconnected : ConnectionState()
    object ConnectFailure : ConnectionState()
}
