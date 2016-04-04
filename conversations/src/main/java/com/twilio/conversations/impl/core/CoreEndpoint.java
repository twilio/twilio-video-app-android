package com.twilio.conversations.impl.core;

import com.twilio.conversations.IceOptions;
import com.twilio.conversations.impl.ConversationImpl;

public interface CoreEndpoint {
    void accept(ConversationImpl conversationImpl, IceOptions iceOptions);

    void reject(ConversationImpl conversationImpl);

    void ignore(ConversationImpl conversationImpl);
}
