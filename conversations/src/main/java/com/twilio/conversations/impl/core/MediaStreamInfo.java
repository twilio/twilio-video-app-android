package com.twilio.conversations.impl.core;

public interface MediaStreamInfo {
    long getSessionId();

    long getStreamId();

    String getParticipantAddress();
}
