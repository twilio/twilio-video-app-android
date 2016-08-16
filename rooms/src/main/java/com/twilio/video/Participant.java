package com.twilio.video;


public class Participant {
    private String identity;
    private String sid;
    private Media media;
    private long nativeParticipantContext;

    Participant(String identity, String sid,
                long nativeMediaContext, long nativeParticipantContext) {
        this.identity = identity;
        this.sid = sid;
        this.media = new Media();
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
        return nativeIsConnected(nativeParticipantContext);
    }

    void release(){
        if (nativeParticipantContext != 0) {
            nativeRelease(nativeParticipantContext);
            nativeParticipantContext = 0;
        }
    }

    private native boolean nativeIsConnected(long nativeHandle);
    private native void nativeRelease(long nativeHandle);
}
