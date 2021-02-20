package com.twilio.video.app.chat

sealed class ChatEvent {
    object ClientConnectFailure : ChatEvent()
}
