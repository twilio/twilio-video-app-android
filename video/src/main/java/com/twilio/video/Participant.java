package com.twilio.video;


public class Participant {
    private String identity;
    private String sid;
    private Media media;
    private long nativeParticipantContext;

    Participant(String identity, String sid,
                Media media, long nativeParticipantContext) {
        this.identity = identity;
        this.sid = sid;
        this.media = media;
        this.nativeParticipantContext = nativeParticipantContext;
    }

    public String getIdentity() {
        return identity;
    }

    public Media getMedia() {
        return media;
    }

    public String getSid() {
        return sid;
    }

    public boolean isConnected() {
        if (!isReleased()) {
            return nativeIsConnected(nativeParticipantContext);
        } else {
            return false;
        }
    }

    void release(){
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
