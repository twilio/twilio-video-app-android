package com.twilio.conversations.core;

public interface MediaStreamInfo {
    long getSessionId();

    long getStreamId();

    String getParticipantAddress();
}
