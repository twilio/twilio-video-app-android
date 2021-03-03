package com.twilio.video.app.chat

sealed class ChatEvent {
    object ClientConnectFailure : ChatEvent()
    object ClientSynchronizationFailure : ChatEvent()
    object ConversationJoinFailure : ChatEvent()
    object GetMessagesFailure : ChatEvent()
    object SendMessageFailure : ChatEvent()
    data class SendMessageSuccess(val message: ChatMessage) : ChatEvent()
}
