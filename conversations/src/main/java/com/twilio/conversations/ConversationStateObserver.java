package com.twilio.conversations;

interface ConversationStateObserver {
    void onConversationStatusChanged(Conversation conversation,
                                     ConversationStatus conversationStatus);
}
