package com.twilio.conversations.impl.core;

import com.twilio.conversations.TrackOrigin;


public class TrackInfoImpl implements TrackInfo {
    private String participantIdentity;
    private String trackId;
    private TrackOrigin trackOrigin;
    private boolean enabled;

    /*
     * Workaround for GSDK-339 - Return an integer from JNI. Release builds were crashing with bad
     * jboolean data when returning a boolean for enabled. Use an integer to mask the crash for now.
     */
    public TrackInfoImpl(String participantIdentity, String trackId, TrackOrigin trackOrigin, int enabled) {
        this.participantIdentity = participantIdentity;
        this.trackId = trackId;
        this.trackOrigin = trackOrigin;
        this.enabled = enabled != 0;
    }

    @Override
    public String getParticipantIdentity() {
        return participantIdentity;
    }

    @Override
    public String getTrackId() {
        return trackId;
    }

    @Override
    public TrackOrigin getTrackOrigin() {
        return trackOrigin;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
