package com.twilio.video;

public class TrackInfo {
    private String participantIdentity;
    private String trackId;
    private TrackOrigin trackOrigin;
    private boolean enabled;

    public TrackInfo(String participantIdentity,
                     String trackId,
                     TrackOrigin trackOrigin,
                     boolean enabled) {
        this.participantIdentity = participantIdentity;
        this.trackId = trackId;
        this.trackOrigin = trackOrigin;
        this.enabled = enabled;
    }

    public String getParticipantIdentity() {
        return participantIdentity;
    }

    public String getTrackId() {
        return trackId;
    }

    public TrackOrigin getTrackOrigin() {
        return trackOrigin;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
