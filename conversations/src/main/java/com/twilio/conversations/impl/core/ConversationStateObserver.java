package com.twilio.conversations.impl.core;

import com.twilio.conversations.Conversation;

public interface ConversationStateObserver {

    void onConversationStatusChanged(Conversation conversation, ConversationStatus conversationStatus);

}
