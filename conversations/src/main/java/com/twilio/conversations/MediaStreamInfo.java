package com.twilio.conversations;

public class MediaStreamInfo {
    private long sessionId;
    private long streamId;
    private String participantAddress;

    public MediaStreamInfo(int sessionId, int streamId, String participantAddress) {
        this.sessionId = sessionId;
        this.streamId = streamId;
        this.participantAddress = participantAddress;
    }

    public long getSessionId() {
        return sessionId;
    }

    public long getStreamId() {
        return streamId;
    }

    public String getParticipantAddress() {
        return participantAddress;
    }
}
