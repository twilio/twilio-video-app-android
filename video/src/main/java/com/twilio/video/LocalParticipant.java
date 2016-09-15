package com.twilio.video;

public class LocalParticipant {

    final private String sid;
    final private String identity;
    final private LocalMedia localMedia;

    LocalParticipant(String sid, String identity, LocalMedia localMedia) {
        this.sid = sid;
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
}
