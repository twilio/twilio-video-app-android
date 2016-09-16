package com.twilio.video;

/**
 * Represents the local participant of a {@link Room} and is provided upon connected.
 */
public class LocalParticipant {
    public final String sid;
    public final String identity;
    public final LocalMedia localMedia;

    LocalParticipant(String sid, String identity, LocalMedia localMedia) {
        this.sid = sid;
        this.identity = identity;
        this.localMedia = localMedia;
    }
}
