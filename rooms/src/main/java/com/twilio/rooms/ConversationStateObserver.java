package com.twilio.rooms;

interface ConversationStateObserver {
    void onConversationStatusChanged(Conversation conversation,
                                     ConversationStatus conversationStatus);
}
