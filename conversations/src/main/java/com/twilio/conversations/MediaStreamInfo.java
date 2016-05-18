package com.twilio.conversations;

public interface MediaStreamInfo {
    long getSessionId();

    long getStreamId();

    String getParticipantAddress();
}
