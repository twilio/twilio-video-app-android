package com.twilio.signal.impl.core;

import com.twilio.signal.Conversation;
import com.twilio.signal.ConversationException;

public interface ConversationStateObserver {

    void onConversationStatusChanged(Conversation conversation, Conversation.Status status);

}
