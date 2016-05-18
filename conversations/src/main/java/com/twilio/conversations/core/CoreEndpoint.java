package com.twilio.conversations.core;

import com.twilio.conversations.Conversation;
import com.twilio.conversations.IceOptions;

public interface CoreEndpoint {
    void accept(Conversation conversationImpl, IceOptions iceOptions);

    void reject(Conversation conversationImpl);

    void ignore(Conversation conversationImpl);
}
