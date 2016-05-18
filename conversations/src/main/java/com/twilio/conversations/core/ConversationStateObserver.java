package com.twilio.conversations.core;

import com.twilio.conversations.Conversation;

public interface ConversationStateObserver {
    void onConversationStatusChanged(Conversation conversation,
                                     ConversationStatus conversationStatus);
}
