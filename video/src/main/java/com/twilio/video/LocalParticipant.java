package com.twilio.video;

/**
 * Represents the local participant of a {@link Room} and is provided upon connected.
 */
public class LocalParticipant {
    private final String sid;
    private final String identity;
    private final LocalMedia localMedia;

    LocalParticipant(String sid, String identity, LocalMedia localMedia) {
        this.sid = sid;
        this.identity = identity;
        this.localMedia = localMedia;
    }

    /**
     * Returns the SID of the local participant.
     */
    public String getSid() {
        return sid;
    }

    /**
     * Returns the identify of the local participant
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * Returns the local media of a local participant.
     */
    public LocalMedia getLocalMedia() {
        return localMedia;
    }
}
