package com.twilio.conversations;

public interface ConversationStateObserver {
    void onConversationStatusChanged(Conversation conversation,
                                     ConversationStatus conversationStatus);
}
