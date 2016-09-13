package com.twilio.video;

public class LocalParticipant {

    private String sid;
    private String identity;
    private LocalMedia localMedia;

    LocalParticipant(String identity, LocalMedia localMedia) {
        this.identity = identity;
        this.localMedia = localMedia;
    }

    public String getSid() {
        return sid;
    }

    public String getIdentity() {
        return identity;
    }

    public LocalMedia getLocalMedia() {
        return localMedia;
    }

    void setSid(String sid) {
        this.sid = sid;
    }
}
