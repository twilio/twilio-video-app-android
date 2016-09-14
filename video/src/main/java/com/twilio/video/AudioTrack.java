package com.twilio.video;

/**
 * An audio track represents a single local or remote audio source
 */
public class AudioTrack implements Track {
    private String trackId;
    private boolean isEnabled;

    AudioTrack(String trackId,
               boolean isEnabled) {
        this.trackId = trackId;
        this.isEnabled = isEnabled;
    }

    /**
     * This audio track id
     * @return track id
     */
    @Override
    public String getTrackId() {
        return trackId;
    }

    /**
     * Check if this audio track is enabled
     * @return true if track is enabled
     */
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
