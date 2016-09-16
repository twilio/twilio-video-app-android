package com.twilio.video;

/**
 * A participant represents a remote user that can connect to a {@link Room}.
 */
public class Participant {
    private final String identity;
    private final String sid;
    private final Media media;
    private long nativeParticipantContext;

    Participant(String identity, String sid, Media media, long nativeParticipantContext) {
        this.identity = identity;
        this.sid = sid;
        this.media = media;
        this.nativeParticipantContext = nativeParticipantContext;
    }

    /**
     * Returns the identity of the participant.
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * Returns the {@link Media} of a participant.
     */
    public Media getMedia() {
        return media;
    }

    /**
     * Returns the SID of a participant.
     */
    public String getSid() {
        return sid;
    }

    /**
     * Checks if the participant is connected to a room.
     *
     * @return true if the participant is connected to a room and false if not.
     */
    public synchronized boolean isConnected() {
        if (!isReleased()) {
            return nativeIsConnected(nativeParticipantContext);
        } else {
            return false;
        }
    }

    synchronized void release(){
        if (!isReleased()) {
            if (media != null) {
                media.release();
            }
            nativeRelease(nativeParticipantContext);
            nativeParticipantContext = 0;
        }
    }

    boolean isReleased() {
        return nativeParticipantContext == 0;
    }

    private native boolean nativeIsConnected(long nativeHandle);
    private native void nativeRelease(long nativeHandle);
}
