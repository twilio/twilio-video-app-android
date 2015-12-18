package com.twilio.conversations.impl.core;

import com.twilio.conversations.Conversation;
import com.twilio.conversations.ConversationException;

public interface ConversationStateObserver {

    void onConversationStatusChanged(Conversation conversation, Conversation.Status status);

}
