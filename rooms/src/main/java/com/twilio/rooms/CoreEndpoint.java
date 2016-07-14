package com.twilio.rooms;

interface CoreEndpoint {
    void accept(Conversation conversation, IceOptions iceOptions);
    void reject(Conversation conversationImpl);
    void ignore(Conversation conversationImpl);
}
